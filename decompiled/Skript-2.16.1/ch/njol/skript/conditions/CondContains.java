package ch.njol.skript.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SimplifiedCondition;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.VerboseAssert;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.common.AnyContains;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import ch.njol.util.StringUtils;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import org.bukkit.event.Event;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;

@Name("Contains")
@Description({"Checks whether an inventory contains an item, a text contains another piece of text, a container contains something, or a list (e.g. {list variable::*} or 'drops') contains another object."})
@Example.Examples({@Example("block contains 20 cobblestone"), @Example("player has 4 flint and 2 iron ingots"), @Example("{list::*} contains 5")})
@Since({"1.0"})
@Deprecated(
   since = "2.13",
   forRemoval = true
)
public class CondContains extends Condition implements VerboseAssert {
   private Expression<?> containers;
   private Expression<?> items;
   private boolean explicitSingle;
   private CondContains.CheckType checkType;

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.containers = LiteralUtils.defendExpression(exprs[0]);
      this.items = LiteralUtils.defendExpression(exprs[1]);
      this.explicitSingle = matchedPattern == 2 && parseResult.mark != 1 || this.containers.isSingle();
      if (matchedPattern <= 1) {
         this.checkType = CondContains.CheckType.INVENTORY;
      } else {
         this.checkType = CondContains.CheckType.UNKNOWN;
      }

      this.setNegated(matchedPattern % 2 == 1);
      return LiteralUtils.canInitSafely(this.containers, this.items);
   }

   @Override
   public boolean check(Event event) {
      CondContains.CheckType checkType = this.checkType;
      Object[] containerValues = this.containers.getAll(event);
      if (containerValues.length == 0) {
         return this.isNegated();
      } else {
         if (checkType == CondContains.CheckType.UNKNOWN) {
            if (Arrays.stream(containerValues).allMatch(Inventory.class::isInstance)) {
               checkType = CondContains.CheckType.INVENTORY;
            } else if (this.explicitSingle
               && Arrays.stream(containerValues)
                  .allMatch(object -> object instanceof AnyContains || Converters.converterExists(object.getClass(), AnyContains.class))) {
               checkType = CondContains.CheckType.CONTAINER;
            } else if (this.explicitSingle && Arrays.stream(containerValues).allMatch(String.class::isInstance)) {
               checkType = CondContains.CheckType.STRING;
            } else {
               checkType = CondContains.CheckType.OBJECTS;
            }
         }
         return switch (checkType) {
            case STRING -> {
               boolean caseSensitive = SkriptConfig.caseSensitive.value();
               yield SimpleExpression.check(containerValues, o -> {
                  String string = (String)o;
                  return this.items.check(event, o1 -> o1 instanceof String text ? StringUtils.contains(string, text, caseSensitive) : false);
               }, this.isNegated(), this.containers.getAnd());
            }
            case INVENTORY -> SimpleExpression.check(containerValues, o -> {
               Inventory inventory = (Inventory)o;
               return this.items.check(event, o1 -> {
                  if (o1 instanceof ItemType type) {
                     return type.isContainedIn(inventory);
                  } else if (o1 instanceof ItemStack stack) {
                     return inventory.containsAtLeast(stack, stack.getAmount());
                  } else {
                     return o1 instanceof Inventory ? Objects.equals(inventory, o1) : false;
                  }
               });
            }, this.isNegated(), this.containers.getAnd());
            default -> {
               assert checkType == CondContains.CheckType.OBJECTS;

               yield this.items.check(event, o1 -> {
                  for (Object o2 : containerValues) {
                     if (Comparators.compare(o1, o2) == Relation.EQUAL) {
                        return true;
                     }
                  }

                  return false;
               }, this.isNegated());
            }
            case CONTAINER -> SimpleExpression.check(containerValues, object -> {
               if (!(object instanceof AnyContains container)) {
                  container = Converters.convert(object, AnyContains.class);
               }

               return container == null ? false : this.items.check(event, container::checkSafely);
            }, this.isNegated(), this.containers.getAnd());
         };
      }
   }

   @Override
   public String getExpectedMessage(Event event) {
      StringJoiner joiner = new StringJoiner(" ");
      joiner.add("to");
      if (this.isNegated()) {
         joiner.add("not");
      }

      joiner.add("find %s".formatted(VerboseAssert.getExpressionValue(this.items, event)));
      return joiner.toString();
   }

   @Override
   public String getReceivedMessage(Event event) {
      StringJoiner joiner = new StringJoiner(" ");
      if (!this.isNegated()) {
         joiner.add("no");
      } else {
         joiner.add("a");
      }

      joiner.add("match in %s".formatted(VerboseAssert.getExpressionValue(this.containers, event)));
      return joiner.toString();
   }

   @Override
   public Condition simplify() {
      return (Condition)(this.containers instanceof Literal && this.items instanceof Literal ? SimplifiedCondition.fromCondition(this) : this);
   }

   @Override
   public String toString(@Nullable Event e, boolean debug) {
      return this.containers.toString(e, debug) + (this.isNegated() ? " doesn't contain " : " contains ") + this.items.toString(e, debug);
   }

   static {
      if (!SkriptConfig.useTypeProperties.value()) {
         Skript.registerCondition(
            CondContains.class,
            "%inventories% (has|have) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
            "%inventories% (doesn't|does not|do not|don't) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
            "%inventories/strings/objects% contain[(1¦s)] %itemtypes/strings/objects%",
            "%inventories/strings/objects% (doesn't|does not|do not|don't) contain %itemtypes/strings/objects%"
         );
      }
   }

   private static enum CheckType {
      STRING,
      INVENTORY,
      OBJECTS,
      UNKNOWN,
      CONTAINER;
   }
}

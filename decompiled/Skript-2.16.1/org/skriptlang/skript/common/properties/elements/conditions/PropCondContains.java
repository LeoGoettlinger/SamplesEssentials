package org.skriptlang.skript.common.properties.elements.conditions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.RelatedProperty;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.VerboseAssert;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.Map.Entry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Comparators;
import org.skriptlang.skript.lang.comparator.Relation;
import org.skriptlang.skript.lang.converter.Converters;
import org.skriptlang.skript.lang.properties.Property;
import org.skriptlang.skript.lang.properties.PropertyBaseSyntax;
import org.skriptlang.skript.lang.properties.PropertyMap;
import org.skriptlang.skript.lang.properties.handlers.ContainsHandler;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Contains (Property)")
@Description({"Checks whether a type or list contains certain elements.\nWhen checking if a list contains a specific element, use '{list::*} contains {x}'.\nWhen checking if a single type contains something, use `player's inventory contains {x}`.\nWhen checking if many types contain something, use '{inventories::*} contain {x}` or `contents of {inventories::*} contain {x}`.\n"})
@Example.Examples({@Example("block contains 20 cobblestone"), @Example("player has 4 flint and 2 iron ingots"), @Example("{list::*} contains 5"), @Example("names of {list::*} contain \"prefix\""), @Example("contents of the inventories of all players contain 1 stick")})
@RelatedProperty("contains")
public class PropCondContains extends Condition implements PropertyBaseSyntax<ContainsHandler<?, ?>>, VerboseAssert {
   private Expression<?> haystack;
   private Expression<?> needles;
   private PropertyMap<ContainsHandler<?, ?>> properties;
   boolean allowContainmentCheck = false;
   boolean allowDirectCheck = false;
   int matchedPattern;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         SyntaxInfo.builder(PropCondContains.class)
            .addPatterns(
               "%objects% contain[1:s] %objects%",
               "%objects% (1:doesn't|1:does not|do not|don't) contain %objects%",
               "contents of %objects% contain %objects%",
               "contents of %objects% (do not|don't) contain %objects%",
               "%inventories% (has|have) %itemtypes% [in [(the[ir]|his|her|its)] inventory]",
               "%inventories% (doesn't|does not|do not|don't) have %itemtypes% [in [(the[ir]|his|her|its)] inventory]"
            )
            .supplier(PropCondContains::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.haystack = LiteralUtils.defendExpression(expressions[0]);
      this.needles = LiteralUtils.defendExpression(expressions[1]);
      this.matchedPattern = matchedPattern;
      if (!LiteralUtils.canInitSafely(this.haystack, this.needles)) {
         return false;
      } else {
         this.allowContainmentCheck = parseResult.mark != 1 || this.haystack.isSingle();
         this.allowDirectCheck = matchedPattern < 2;
         this.setNegated(matchedPattern % 2 == 1);
         if (!this.allowContainmentCheck) {
            return this.initDirect(null);
         } else {
            Expression<?> tempHaystack = PropertyBaseSyntax.asProperty(Property.CONTAINS, expressions[0]);
            if (tempHaystack == null) {
               return this.initDirect(this.getBadTypesErrorMessage(expressions[0]));
            } else {
               this.properties = PropertyBaseSyntax.getPossiblePropertyInfos(Property.CONTAINS, tempHaystack);
               if (this.properties.isEmpty()) {
                  return this.initDirect(this.getBadTypesErrorMessage(tempHaystack));
               } else {
                  Class<?>[][] elementTypes = this.getElementTypes(this.properties);
                  Class<?>[] elementTypeSet = Arrays.stream(elementTypes).flatMap(Arrays::stream).distinct().toArray(Class[]::new);

                  for (Class<?> type : elementTypeSet) {
                     if (type == Object.class) {
                        elementTypeSet = new Class[]{Object.class};
                        break;
                     }
                  }

                  Expression convertedNeedles = this.needles.getConvertedExpression(elementTypeSet);
                  if (convertedNeedles == null) {
                     return this.initDirect(
                        "'"
                           + tempHaystack
                           + "' cannot contain "
                           + Classes.toString(Arrays.stream(this.needles.possibleReturnTypes()).map(Classes::getSuperClassInfo).toArray(), false)
                     );
                  } else {
                     this.needles = convertedNeedles;
                     return LiteralUtils.canInitSafely(this.haystack, this.needles);
                  }
               }
            }
         }
      }
   }

   private boolean initDirect(@Nullable String error) {
      boolean validType = false;

      label39:
      for (Class<?> haystackType : this.haystack.possibleReturnTypes()) {
         for (Class<?> needleType : this.needles.possibleReturnTypes()) {
            if (haystackType == Object.class || needleType == Object.class || Comparators.comparatorExists(haystackType, needleType)) {
               validType = true;
               break label39;
            }
         }
      }

      if (!validType) {
         Skript.error(
            error != null ? error : "'" + this.haystack.toString() + "' cannot contain " + Classes.toString(this.needles.possibleReturnTypes(), false)
         );
         return false;
      } else {
         this.allowContainmentCheck = false;
         return LiteralUtils.canInitSafely(this.haystack, this.needles);
      }
   }

   private Class<?>[][] getElementTypes(PropertyMap<ContainsHandler<?, ?>> properties) {
      return properties.values().stream().map(propertyInfo -> propertyInfo.handler().elementTypes()).toArray(Class[][]::new);
   }

   @Override
   public boolean check(Event event) {
      Object[] haystacks = this.haystack.getAll(event);
      boolean haystackAnd = this.haystack.getAnd();
      Object[] needles = this.needles.getAll(event);
      boolean needlesAnd = this.needles.getAnd();
      if (haystacks.length == 0) {
         return this.isNegated();
      } else {
         return this.allowContainmentCheck
            ? this.checkContainment(haystacks, haystackAnd, needles, needlesAnd)
            : this.checkDirect(haystacks, needles, needlesAnd);
      }
   }

   private boolean checkDirect(Object[] haystacks, Object[] needles, boolean needlesAnd) {
      return SimpleExpression.check(needles, o1 -> {
         for (Object o2 : haystacks) {
            if (Comparators.compare(o1, o2) == Relation.EQUAL) {
               return true;
            }
         }

         return false;
      }, this.isNegated(), needlesAnd);
   }

   @Nullable
   private Object convert(Object input, Class<?>... targets) {
      Class<?> type = input.getClass();
      return this.properties.getHandler(type) != null ? input : Converters.convert(input, targets);
   }

   private boolean checkContainment(Object[] haystacks, boolean haystackAnd, Object[] needles, boolean needlesAnd) {
      boolean allHaveProperty = true;
      Class<?>[] targetTypes = this.properties.entrySet().stream().filter(entry -> entry.getValue() != null).map(Entry::getKey).toArray(Class[]::new);
      Object[] convertedHaystacks = new Object[haystacks.length];

      for (int i = 0; i < haystacks.length; i++) {
         convertedHaystacks[i] = this.convert(haystacks[i], targetTypes);
         if (convertedHaystacks[i] == null) {
            allHaveProperty = false;
            break;
         }
      }

      if (!allHaveProperty) {
         return this.allowDirectCheck ? this.checkDirect(haystacks, needles, needlesAnd) : this.isNegated();
      } else {
         return SimpleExpression.check(
            convertedHaystacks,
            haystack -> {
               ContainsHandler<Object, Object> handler = (ContainsHandler<Object, Object>)this.properties.getHandler(haystack.getClass());
               return handler == null
                  ? false
                  : SimpleExpression.check(needles, needle -> handler.canContain(needle.getClass()) && handler.contains(haystack, needle), false, needlesAnd);
            },
            this.isNegated(),
            haystackAnd
         );
      }
   }

   @NotNull
   @Override
   public Property<ContainsHandler<?, ?>> getProperty() {
      return Property.CONTAINS;
   }

   @Override
   public String getExpectedMessage(Event event) {
      StringJoiner joiner = new StringJoiner(" ");
      joiner.add("to");
      if (this.isNegated()) {
         joiner.add("not");
      }

      joiner.add("find %s".formatted(VerboseAssert.getExpressionValue(this.needles, event)));
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

      joiner.add("match in %s".formatted(VerboseAssert.getExpressionValue(this.haystack, event)));
      return joiner.toString();
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      switch (this.matchedPattern) {
         case 1:
         case 2:
            builder.append(this.haystack);
            if (this.isNegated()) {
               builder.append(this.allowContainmentCheck ? "don't" : "doesn't");
            }

            builder.append(this.allowContainmentCheck ? "contain" : "contains").append(this.needles);
            break;
         case 3:
         case 4:
            builder.append("contents of", this.haystack);
            if (this.isNegated()) {
               builder.append("don't");
            }

            builder.append("contain", this.needles);
            break;
         case 5:
         case 6:
            builder.append(this.haystack);
            if (this.isNegated()) {
               builder.append("don't");
            }

            builder.append("have", this.needles);
      }

      return builder.toString();
   }
}

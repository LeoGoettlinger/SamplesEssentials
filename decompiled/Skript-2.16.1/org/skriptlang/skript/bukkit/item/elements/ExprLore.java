package org.skriptlang.skript.bukkit.item.elements;

import ch.njol.skript.SkriptConfig;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionList;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.SyntaxStringBuilder;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TextReplacementConfig.Builder;
import org.bukkit.event.Event;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.text.TextComponentParser;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Lore")
@Description({"Returns the lore of an item."})
@Example("set the 1st line of the item's lore to \"<orange>Excalibur 2.0\"")
@Since({"2.1"})
public class ExprLore extends SimpleExpression<Component> {
   @Nullable
   private Expression<Number> line;
   private Expression<ItemType> item;

   public static void register(SyntaxRegistry syntaxRegistry) {
      syntaxRegistry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprLore.class, Component.class
                     )
                     .supplier(ExprLore::new))
                  .priority(PropertyExpression.DEFAULT_PRIORITY))
               .addPatterns(
                  new String[]{
                     "[the] lore of %itemtype%",
                     "%itemtype%'[s] lore",
                     "[the] line %number% of [the] lore of %itemtype%",
                     "[the] line %number% of %itemtype%'[s] lore",
                     "[the] %number%(st|nd|rd|th) line of [the] lore of %itemtype%",
                     "[the] %number%(st|nd|rd|th) line of %itemtype%'[s] lore"
                  }
               ))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.line = (Expression<Number>)(exprs.length > 1 ? exprs[0] : null);
      this.item = (Expression<ItemType>)exprs[exprs.length - 1];
      return true;
   }

   @Nullable
   protected Component[] get(Event event) {
      ItemType itemType = this.item.getSingle(event);
      if (itemType == null) {
         return null;
      } else {
         ItemMeta itemMeta = itemType.getItemMeta();
         if (!itemMeta.hasLore()) {
            return new Component[0];
         } else {
            List<Component> lore = itemMeta.lore();

            assert lore != null;

            if (this.line == null) {
               return lore.toArray(new Component[0]);
            } else {
               int line = this.line.getOptionalSingle(event).orElse(0).intValue() - 1;
               return line >= 0 && line < lore.size() ? new Component[]{lore.get(line)} : new Component[0];
            }
         }
      }
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, SET -> this.line == null
            ? (Class[])CollectionUtils.array(Component[].class, String[].class)
            : (Class[])CollectionUtils.array(Component.class);
         case REMOVE, DELETE, REMOVE_ALL -> (Class[])CollectionUtils.array(this.line == null ? Component.class : String.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      ItemType itemType = this.item.getSingle(event);
      if (itemType != null) {
         ItemMeta itemMeta = itemType.getItemMeta();
         List<Component> lore;
         if (itemMeta.hasLore()) {
            lore = itemMeta.lore();
         } else {
            lore = new ArrayList<>();
         }

         assert lore != null;

         if (this.line == null) {
            switch (mode) {
               case ADD:
                  assert delta != null;

                  lore.addAll(parseLore(delta));
                  break;
               case SET:
                  assert delta != null;

                  lore = parseLore(delta);
                  break;
               case REMOVE:
               case REMOVE_ALL:
                  assert delta != null;

                  if (mode == Changer.ChangeMode.REMOVE_ALL) {
                     lore.removeIf(component -> component.equals(delta[0]));
                  } else {
                     lore.remove((Component)delta[0]);
                  }
                  break;
               case DELETE:
                  lore = null;
                  break;
               default:
                  assert false;

                  return;
            }
         } else {
            int line = this.line.getOptionalSingle(event).orElse(0).intValue() - 1;
            if (line < 0) {
               return;
            }

            while (lore.size() <= line) {
               lore.add(Component.empty());
            }

            switch (mode) {
               case ADD:
                  assert delta != null;

                  lore.set(line, lore.get(line).append((Component)delta[0]));
                  break;
               case SET:
                  assert delta != null;

                  lore.set(line, (Component)delta[0]);
                  break;
               case REMOVE:
               case REMOVE_ALL:
                  assert delta != null;

                  Builder builder = TextReplacementConfig.builder();
                  if (mode == Changer.ChangeMode.REMOVE) {
                     builder.once();
                  }

                  int flags = SkriptConfig.caseSensitive.value() ? 0 : 66;
                  builder.match(Pattern.compile(Pattern.quote((String)delta[0]), flags));
                  builder.replacement(Component.empty());
                  lore.set(line, lore.get(line).replaceText((TextReplacementConfig)builder.build()));
                  break;
               case DELETE:
                  lore.remove(line);
                  break;
               default:
                  assert false;

                  return;
            }
         }

         itemMeta.lore(lore);
         itemType.setItemMeta(itemMeta);
      }
   }

   @Override
   public boolean isSingle() {
      return this.line != null;
   }

   @Override
   public Class<? extends Component> getReturnType() {
      return Component.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      SyntaxStringBuilder builder = new SyntaxStringBuilder(event, debug);
      if (this.line != null) {
         builder.append("the line", this.line, "of");
      }

      builder.append("the lore of", this.item);
      return builder.toString();
   }

   static Expression<?> convertStrings(Expression<?> expression) {
      boolean isString = String.class.isAssignableFrom(expression.getReturnType());
      if (expression instanceof ExpressionList<?> list) {
         boolean hasComponent = false;
         Expression<?>[] expressions = list.getExpressions();

         for (int i = 0; i < expressions.length; i++) {
            expressions[i] = convertStrings(expressions[i]);
            hasComponent |= Component.class.isAssignableFrom(expressions[i].getReturnType());
         }

         if (isString && hasComponent) {
            return new ExpressionList<>(expressions, Object.class, list.getAnd());
         }
      } else if (isString && expression instanceof Literal<?> string) {
         return string.getConvertedExpression(Component.class);
      }

      return expression;
   }

   static List<Component> parseLore(Object[] lore) {
      List<Component> loreList = new ArrayList<>();

      for (Object line : lore) {
         if (line instanceof Component component) {
            loreList.add(component);
         } else {
            for (String textLine : ((String)line).split("\n")) {
               loreList.add(TextComponentParser.instance().parseSafe(textLine));
            }
         }
      }

      return loreList;
   }
}

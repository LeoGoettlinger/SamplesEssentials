package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Events;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EventRestrictedSyntax;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Applied Enchantments")
@Description({"The applied enchantments in an enchant event.\nDeleting or removing the applied enchantments will prevent the item's enchantment.\n"})
@Example("on enchant:\n\tset the applied enchantments to sharpness 10 and fire aspect 5\n")
@Events({"enchant"})
@Since({"2.5"})
public class ExprAppliedEnchantments extends SimpleExpression<EnchantmentType> implements EventRestrictedSyntax {
   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                     ExprAppliedEnchantments.class, EnchantmentType.class
                  )
                  .addPattern("[the] applied enchant[ment]s"))
               .supplier(ExprAppliedEnchantments::new))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      return true;
   }

   @Override
   public Class<? extends Event>[] supportedEvents() {
      return CollectionUtils.array(EnchantItemEvent.class);
   }

   @Nullable
   protected EnchantmentType[] get(Event event) {
      return event instanceof EnchantItemEvent enchantEvent
         ? enchantEvent.getEnchantsToAdd()
            .entrySet()
            .stream()
            .map(entry -> new EnchantmentType((Enchantment)entry.getKey(), (Integer)entry.getValue()))
            .toArray(EnchantmentType[]::new)
         : new EnchantmentType[0];
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case ADD, SET, DELETE, REMOVE -> (Class[])CollectionUtils.array(EnchantmentType[].class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      if (event instanceof EnchantItemEvent enchantEvent) {
         EnchantmentType[] enchants = new EnchantmentType[delta != null ? delta.length : 0];
         if (delta != null && delta.length != 0) {
            for (int i = 0; i < delta.length; i++) {
               enchants[i] = (EnchantmentType)delta[i];
            }
         }

         if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE) {
            enchantEvent.getEnchantsToAdd().clear();
         }

         switch (mode) {
            case ADD:
            case SET:
               for (EnchantmentType enchant : enchants) {
                  enchantEvent.getEnchantsToAdd().put(enchant.getType(), enchant.getLevel());
               }
            case DELETE:
               break;
            case REMOVE:
               for (EnchantmentType enchant : enchants) {
                  enchantEvent.getEnchantsToAdd().remove(enchant.getType(), enchant.getLevel());
               }
               break;
            default:
               assert false;
         }
      }
   }

   @Override
   public boolean isSingle() {
      return false;
   }

   @Override
   public Class<? extends EnchantmentType> getReturnType() {
      return EnchantmentType.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the applied enchantments";
   }
}

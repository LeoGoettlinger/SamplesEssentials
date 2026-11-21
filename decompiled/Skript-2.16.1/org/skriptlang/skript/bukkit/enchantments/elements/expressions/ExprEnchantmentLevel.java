package org.skriptlang.skript.bukkit.enchantments.elements.expressions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import ch.njol.util.Math2;
import ch.njol.util.coll.CollectionUtils;
import java.util.stream.Stream;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.DefaultSyntaxInfos;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Enchantment Level")
@Description({"The level of a particular <a href='#enchantment'>enchantment</a> on an item."})
@Example("player's tool is a sword of sharpness:\n\tmessage \"You have a sword of sharpness %level of sharpness of the player's tool% equipped\"\n")
@Since({"2.0"})
public class ExprEnchantmentLevel extends SimpleExpression<Long> {
   private Expression<ItemType> items;
   private Expression<Enchantment> enchants;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.EXPRESSION,
         ((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)((DefaultSyntaxInfos.Expression.Builder)DefaultSyntaxInfos.Expression.builder(
                        ExprEnchantmentLevel.class, Long.class
                     )
                     .addPatterns(
                        new String[]{
                           "[the] [enchant[ment]] level[s] of %enchantments% (on|of) %itemtypes%",
                           "[the] %enchantments% [enchant[ment]] level[s] (on|of) %itemtypes%",
                           "%itemtypes%'[s] %enchantments% [enchant[ment]] level[s]",
                           "%itemtypes%'[s] [enchant[ment]] level[s] of %enchantments%"
                        }
                     ))
                  .supplier(ExprEnchantmentLevel::new))
               .priority(PropertyExpression.DEFAULT_PRIORITY))
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      int i = matchedPattern < 2 ? 1 : 0;
      this.items = (Expression<ItemType>)exprs[i];
      this.enchants = (Expression<Enchantment>)exprs[i ^ 1];
      return true;
   }

   protected Long[] get(Event event) {
      Enchantment[] enchantments = this.enchants.getArray(event);
      return Stream.of(this.items.getArray(event))
         .map(ItemType::getEnchantmentTypes)
         .flatMap(Stream::of)
         .filter(enchantment -> CollectionUtils.contains(enchantments, enchantment.getType()))
         .map(EnchantmentType::getLevel)
         .map(i -> (long)i.intValue())
         .toArray(Long[]::new);
   }

   @Nullable
   @Override
   public Class<?>[] acceptChange(Changer.ChangeMode mode) {
      return switch (mode) {
         case SET, REMOVE, ADD -> (Class[])CollectionUtils.array(Number.class);
         default -> null;
      };
   }

   @Override
   public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
      ItemType[] itemTypes = this.items.getArray(event);
      Enchantment[] enchantments = this.enchants.getArray(event);

      assert delta != null;

      long changeValue = ((Number)delta[0]).longValue();

      for (ItemType itemType : itemTypes) {
         for (Enchantment enchantment : enchantments) {
            EnchantmentType enchantmentType = itemType.getEnchantmentType(enchantment);
            long oldLevel = enchantmentType == null ? 0L : enchantmentType.getLevel();
            long newItemLevel;
            switch (mode) {
               case SET:
                  newItemLevel = changeValue;
                  break;
               case REMOVE:
                  newItemLevel = Math2.addSaturated(oldLevel, -changeValue);
                  break;
               case ADD:
                  newItemLevel = Math2.addSaturated(oldLevel, changeValue);
                  break;
               default:
                  assert false;

                  return;
            }

            if (newItemLevel <= 0L) {
               itemType.removeEnchantments(new EnchantmentType(enchantment));
            } else {
               itemType.addEnchantments(new EnchantmentType(enchantment, Math2.clampToInt(newItemLevel)));
            }
         }
      }
   }

   @Override
   public boolean isSingle() {
      return this.items.isSingle() && this.enchants.isSingle();
   }

   @Override
   public Class<? extends Long> getReturnType() {
      return Long.class;
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return "the level of " + this.enchants.toString(event, debug) + " of " + this.items.toString(event, debug);
   }
}

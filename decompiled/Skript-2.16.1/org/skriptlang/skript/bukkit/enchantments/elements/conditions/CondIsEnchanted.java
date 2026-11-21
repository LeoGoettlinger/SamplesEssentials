package org.skriptlang.skript.bukkit.enchantments.elements.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.EnchantmentType;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Is Enchanted")
@Description({"Checks whether an item is enchanted. Enchants must match the exact level by default, unless 'or better' or 'or worse' are used."})
@Example.Examples({@Example("tool of the player is enchanted with efficiency 2"), @Example("if player's helmet or player's boots are enchanted with protection 3 or better:"), @Example("if player's chestplate is enchanted with protection")})
@Since({"1.4.6, 2.12 ('or better')"})
public class CondIsEnchanted extends Condition {
   private Expression<ItemType> items;
   @Nullable
   private Expression<EnchantmentType> enchs;
   private CondIsEnchanted.Comparison comparison;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         PropertyCondition.infoBuilder(
               CondIsEnchanted.class,
               PropertyCondition.PropertyType.BE,
               "enchanted [with %-enchantmenttypes% [or (1:(better|greater|higher|above)|2:(worse|lesser|lower|below))]]",
               "itemtypes"
            )
            .supplier(CondIsEnchanted::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      this.items = (Expression<ItemType>)exprs[0];
      this.enchs = (Expression<EnchantmentType>)exprs[1];
      this.comparison = CondIsEnchanted.Comparison.values()[parseResult.mark];
      this.setNegated(matchedPattern == 1);
      return true;
   }

   @Override
   public boolean check(Event event) {
      if (this.enchs != null) {
         EnchantmentType[] enchantments = this.enchs.getAll(event);
         boolean and = this.enchs.getAnd();
         return this.items.check(event, item -> {
            return switch (this.comparison) {
               case EXACT -> item.hasExactEnchantments(and, enchantments);
               case AT_LEAST -> item.hasEnchantmentsOrBetter(and, enchantments);
               case AT_MOST -> item.hasEnchantmentsOrWorse(and, enchantments);
            };
         }, this.isNegated());
      } else {
         return this.items.check(event, ItemType::hasEnchantments, this.isNegated());
      }
   }

   @Override
   public String toString(@Nullable Event event, boolean debug) {
      return PropertyCondition.toString(
         this,
         PropertyCondition.PropertyType.BE,
         event,
         debug,
         this.items,
         "enchanted" + (this.enchs == null ? "" : " with " + this.enchs.toString(event, debug)) + this.comparison.toSkriptString()
      );
   }

   private static enum Comparison {
      EXACT(""),
      AT_LEAST(" or better"),
      AT_MOST(" or worse");

      private final String toString;

      private Comparison(String toString) {
         this.toString = toString;
      }

      public String toSkriptString() {
         return this.toString;
      }
   }
}

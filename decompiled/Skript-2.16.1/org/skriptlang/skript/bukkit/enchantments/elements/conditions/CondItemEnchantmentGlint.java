package org.skriptlang.skript.bukkit.enchantments.elements.conditions;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.conditions.base.PropertyCondition;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.inventory.meta.ItemMeta;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Item Has Enchantment Glint Override")
@Description({"Checks whether an item has the enchantment glint overridden, or is forced to glint or not."})
@Example.Examples({@Example("if the player's tool has the enchantment glint override\n\tsend \"Your tool has the enchantment glint override.\" to player\n"), @Example("if {_item} is forced to glint:\n\tsend \"This item is forced to glint.\" to player\nelse if {_item} is forced to not glint:\n\tsend \"This item is forced to not glint.\" to player\nelse:\n\tsend \"This item does not have any glint override.\" to player\n")})
@Since({"2.10"})
public class CondItemEnchantmentGlint extends PropertyCondition<ItemType> {
   private boolean expectedGlintOverride;
   private boolean override;

   public static void register(SyntaxRegistry registry) {
      registry.register(
         SyntaxRegistry.CONDITION,
         PropertyCondition.infoBuilder(CondItemEnchantmentGlint.class, PropertyCondition.PropertyType.HAVE, "enchantment glint overrid(den|e)", "itemtypes")
            .addPatterns(getPatterns(PropertyCondition.PropertyType.BE, "forced to [:not] glint", "itemtypes"))
            .supplier(CondItemEnchantmentGlint::new)
            .build()
      );
   }

   @Override
   public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
      if (!super.init(expressions, matchedPattern, isDelayed, parseResult)) {
         return false;
      } else {
         this.override = matchedPattern <= 1;
         this.expectedGlintOverride = !parseResult.hasTag("not");
         this.setNegated(matchedPattern % 2 != 0);
         return true;
      }
   }

   public boolean check(ItemType itemType) {
      ItemMeta meta = itemType.getItemMeta();
      if (this.override) {
         return meta.hasEnchantmentGlintOverride();
      } else {
         return !meta.hasEnchantmentGlintOverride() ? false : meta.getEnchantmentGlintOverride() == this.expectedGlintOverride;
      }
   }

   @Override
   protected PropertyCondition.PropertyType getPropertyType() {
      return this.override ? PropertyCondition.PropertyType.HAVE : PropertyCondition.PropertyType.BE;
   }

   @Override
   protected String getPropertyName() {
      return this.override ? "enchantment glint overridden" : "forced to " + (this.expectedGlintOverride ? "" : "not ") + "glint";
   }
}

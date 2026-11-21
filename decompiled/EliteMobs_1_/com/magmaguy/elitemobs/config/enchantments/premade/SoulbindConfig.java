package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public class SoulbindConfig extends EnchantmentsConfigFields {
   public static String loreStrings;
   public static String hologramStrings;

   public SoulbindConfig() {
      super("soulbind", true, "Soulbind", 1, 10.0, true, 1);
   }

   @Override
   public void processAdditionalFields() {
      loreStrings = this.translatable(this.filename, "loreStrings", this.processString("loreStrings", "&7Soulbound to &f$player", null, true));
      hologramStrings = this.translatable(this.filename, "hologramString", this.processString("hologramString", "$player&f's", null, true));
   }
}

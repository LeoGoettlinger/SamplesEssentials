package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public final class BlastRadiusConfig extends EnchantmentsConfigFields {
   private static double generationChance = 0.3;

   public BlastRadiusConfig() {
      super("blast_radius", true, "Blast Radius", 3, 14.0, true, 3);
   }

   @Override
   public void processAdditionalFields() {
      generationChance = Math.max(0.0, Math.min(1.0, this.processDouble("generationChance", 0.3, 0.3, true)));
      if (!Double.isFinite(generationChance)) {
         generationChance = 0.3;
      }
   }

   public static double generationChance() {
      return generationChance;
   }
}

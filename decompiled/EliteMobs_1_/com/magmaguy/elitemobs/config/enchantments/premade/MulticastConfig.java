package com.magmaguy.elitemobs.config.enchantments.premade;

import com.magmaguy.elitemobs.config.enchantments.EnchantmentsConfigFields;

public final class MulticastConfig extends EnchantmentsConfigFields {
   private static double generationChance = 0.35;
   private static double rareLevelChance = 0.01;
   private static int rareMinimumLevel = 75;

   public MulticastConfig() {
      super("multicast", true, "Multicast", 3, 18.0, true, 3);
   }

   @Override
   public void processAdditionalFields() {
      generationChance = Math.max(0.0, Math.min(1.0, this.processDouble("generationChance", 0.35, 0.35, true)));
      rareLevelChance = Math.max(0.0, Math.min(1.0, this.processDouble("levelThreeChance", 0.01, 0.01, true)));
      rareMinimumLevel = Math.max(1, this.processInt("levelThreeMinimumItemLevel", 75, 75, true));
      if (!Double.isFinite(generationChance)) {
         generationChance = 0.35;
      }

      if (!Double.isFinite(rareLevelChance)) {
         rareLevelChance = 0.01;
      }
   }

   public static double generationChance() {
      return generationChance;
   }

   public static double rareLevelChance() {
      return rareLevelChance;
   }

   public static int rareMinimumLevel() {
      return rareMinimumLevel;
   }
}

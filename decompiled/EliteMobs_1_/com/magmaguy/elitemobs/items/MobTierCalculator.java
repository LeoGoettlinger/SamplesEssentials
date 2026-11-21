package com.magmaguy.elitemobs.items;

import com.magmaguy.elitemobs.mobconstructor.EliteEntity;

public class MobTierCalculator {
   public static final double PER_TIER_LEVEL_INCREASE = 1.0;

   private MobTierCalculator() {
   }

   public static double findMobTier(EliteEntity eliteEntity) {
      return eliteEntity.getLevel() / 1.0;
   }

   public static double findMobTier(int mobLevel) {
      return mobLevel / 1.0;
   }

   public static int findMobLevel(int mobTier) {
      return (int)(mobTier * 1.0);
   }
}

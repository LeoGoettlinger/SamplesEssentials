package com.magmaguy.elitemobs.advancedcombat.abilities;

public final class AbilityLevelScaling {
   private static final double BONUS_PER_LEVEL = 0.0025;

   private AbilityLevelScaling() {
   }

   public static double multiplier(int effectiveLevel) {
      return 1.0 + Math.max(1, effectiveLevel) * 0.0025;
   }
}

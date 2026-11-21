package com.magmaguy.elitemobs.advancedcombat;

public final class AdvancedCombatRules {
   public static final double VANILLA_BASE_MAX_HEALTH = 20.0;
   public static final double ADVANCED_BASE_MAX_HEALTH = 50.0;
   public static final double FOOD_SATURATION_TO_HEALTH = 1.0;
   public static final double CAKE_SLICE_SATURATION = 0.4;
   public static final double OUT_OF_COMBAT_HEAL_FRACTION_PER_SECOND = 0.01;
   public static final int SERVER_FOOD_LEVEL = 19;
   public static final int DISPLAY_FOOD_LEVEL = 20;
   public static final int RECONCILIATION_INTERVAL_TICKS = 20;
   public static final long COMBAT_TIMEOUT_TICKS = 400L;

   private AdvancedCombatRules() {
   }

   public static double maxHealthFlatIncrease() {
      return 30.0;
   }

   public static double advancedMaximumHealth(double ordinaryMaximumHealth) {
      return ordinaryMaximumHealth + maxHealthFlatIncrease();
   }

   public static double ordinaryMaximumHealth(double advancedMaximumHealth) {
      return Math.max(1.0, advancedMaximumHealth - maxHealthFlatIncrease());
   }
}

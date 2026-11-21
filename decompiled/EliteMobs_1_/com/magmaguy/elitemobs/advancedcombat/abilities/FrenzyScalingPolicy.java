package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;

public final class FrenzyScalingPolicy {
   public static final double BASE_MAXIMUM_SPEED_ADJUSTMENT = 0.4;

   private FrenzyScalingPolicy() {
   }

   public static int durationTicks(AbilityTuning tuning, int effectiveLevel) {
      Objects.requireNonNull(tuning, "tuning");
      return ActiveAbilityLevelScaling.durationTicks(tuning.durationTicks(), effectiveLevel);
   }

   public static FrenzyScalingPolicy.Scaling scale(double currentHealth, double maximumHealth, double maximumDamageBonus, double maximumSpeedAdjustment) {
      if (Double.isFinite(maximumHealth) && !(maximumHealth <= 0.0)) {
         requireNonNegative(maximumDamageBonus, "maximumDamageBonus");
         requireNonNegative(maximumSpeedAdjustment, "maximumSpeedAdjustment");
         double boundedHealth = Double.isFinite(currentHealth) ? Math.max(0.0, Math.min(maximumHealth, currentHealth)) : maximumHealth;
         double missingHealthFraction = 1.0 - boundedHealth / maximumHealth;
         return new FrenzyScalingPolicy.Scaling(
            missingHealthFraction, 1.0 + maximumDamageBonus * missingHealthFraction, maximumSpeedAdjustment * missingHealthFraction
         );
      } else {
         throw new IllegalArgumentException("maximumHealth must be positive and finite");
      }
   }

   private static void requireNonNegative(double value, String field) {
      if (!Double.isFinite(value) || value < 0.0) {
         throw new IllegalArgumentException(field + " must be finite and non-negative");
      }
   }

   public record Scaling(double missingHealthFraction, double damageMultiplier, double movementSpeedAdjustment) {
   }
}

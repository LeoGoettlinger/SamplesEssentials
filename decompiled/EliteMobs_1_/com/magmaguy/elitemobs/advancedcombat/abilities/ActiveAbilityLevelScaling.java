package com.magmaguy.elitemobs.advancedcombat.abilities;

public final class ActiveAbilityLevelScaling {
   private static final double MINIMUM_REDUCTION_MULTIPLIER = 0.05;

   private ActiveAbilityLevelScaling() {
   }

   public static double amount(double authoredAmount, int effectiveLevel) {
      requireNonNegative(authoredAmount, "authoredAmount");
      return authoredAmount * AbilityLevelScaling.multiplier(effectiveLevel);
   }

   public static double displacement(double authoredDisplacement, int effectiveLevel) {
      return amount(authoredDisplacement, effectiveLevel);
   }

   public static int durationTicks(int authoredTicks, int effectiveLevel) {
      if (authoredTicks < 0) {
         throw new IllegalArgumentException("authoredTicks must not be negative");
      } else if (authoredTicks == 0) {
         return 0;
      } else {
         double scaled = authoredTicks * AbilityLevelScaling.multiplier(effectiveLevel);
         return (int)Math.min(2147483647L, Math.max(1L, Math.round(scaled)));
      }
   }

   public static double modifier(double authoredMultiplier, int effectiveLevel) {
      requireNonNegative(authoredMultiplier, "authoredMultiplier");
      double scale = AbilityLevelScaling.multiplier(effectiveLevel);
      if (authoredMultiplier > 1.0) {
         return 1.0 + (authoredMultiplier - 1.0) * scale;
      } else {
         return authoredMultiplier < 1.0 ? Math.max(0.05, 1.0 - (1.0 - authoredMultiplier) * scale) : 1.0;
      }
   }

   private static void requireNonNegative(double value, String field) {
      if (!Double.isFinite(value) || value < 0.0) {
         throw new IllegalArgumentException(field + " must be finite and non-negative");
      }
   }
}

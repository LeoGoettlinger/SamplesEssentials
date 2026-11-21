package com.magmaguy.elitemobs.advancedcombat.abilities;

public record EliteControlEffectPlan(int durationTicks, int potionAmplifier, double additionalMovementSpeedAdjustment) {
   public EliteControlEffectPlan(int durationTicks, int potionAmplifier, double additionalMovementSpeedAdjustment) {
      if (durationTicks < 0) {
         throw new IllegalArgumentException("durationTicks must not be negative");
      } else if (potionAmplifier < 0 || potionAmplifier > 255) {
         throw new IllegalArgumentException("potionAmplifier must be between 0 and 255");
      } else if (Double.isFinite(additionalMovementSpeedAdjustment)
         && !(additionalMovementSpeedAdjustment > 0.0)
         && !(additionalMovementSpeedAdjustment < -0.95)) {
         this.durationTicks = durationTicks;
         this.potionAmplifier = potionAmplifier;
         this.additionalMovementSpeedAdjustment = additionalMovementSpeedAdjustment;
      } else {
         throw new IllegalArgumentException("additionalMovementSpeedAdjustment must be finite and between -0.95 and 0");
      }
   }

   public static EliteControlEffectPlan slow(int authoredAmplifier, int levelScaledDurationTicks, AbilityMechanicModifiers modifiers) {
      if (authoredAmplifier < 0) {
         throw new IllegalArgumentException("authoredAmplifier must not be negative");
      } else if (levelScaledDurationTicks < 0) {
         throw new IllegalArgumentException("levelScaledDurationTicks must not be negative");
      } else if (modifiers == null) {
         throw new IllegalArgumentException("modifiers must not be null");
      } else {
         double desiredReduction = Math.min(0.95, 0.15 * (authoredAmplifier + 1.0) * modifiers.controlPotencyMultiplier());
         int wholePotionTiers = Math.max(1, Math.min(6, (int)Math.floor(desiredReduction / 0.15 + 1.0E-9)));
         double potionReduction = wholePotionTiers * 0.15;
         double potionMultiplier = Math.max(0.05, 1.0 - potionReduction);
         double desiredMultiplier = Math.max(0.05, 1.0 - desiredReduction);
         double remainingAdjustment = Math.max(-0.95, desiredMultiplier / potionMultiplier - 1.0);
         return new EliteControlEffectPlan(
            scaledDuration(levelScaledDurationTicks, modifiers.controlDurationMultiplier()), wholePotionTiers - 1, remainingAdjustment
         );
      }
   }

   public double movementSpeedMultiplier() {
      double potionAdjustment = -0.15 * (this.potionAmplifier + 1.0);
      return Math.max(0.0, (1.0 + potionAdjustment) * (1.0 + this.additionalMovementSpeedAdjustment));
   }

   private static int scaledDuration(int authoredTicks, double multiplier) {
      return authoredTicks <= 0 ? 0 : (int)Math.min(2147483647L, Math.max(1L, Math.round(authoredTicks * multiplier)));
   }
}

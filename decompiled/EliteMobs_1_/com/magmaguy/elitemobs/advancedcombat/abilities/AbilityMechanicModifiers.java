package com.magmaguy.elitemobs.advancedcombat.abilities;

public record AbilityMechanicModifiers(
   double burstHealingMultiplier,
   double periodicHealingMultiplier,
   double groupedEnemyHealingMultiplier,
   double periodicDurationMultiplier,
   double shieldStrengthMultiplier,
   double redirectedDamageMultiplier,
   double controlDurationMultiplier,
   double controlPotencyMultiplier
) {
   public static final AbilityMechanicModifiers NEUTRAL = new AbilityMechanicModifiers(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);

   public AbilityMechanicModifiers(
      double burstHealingMultiplier,
      double periodicHealingMultiplier,
      double groupedEnemyHealingMultiplier,
      double periodicDurationMultiplier,
      double shieldStrengthMultiplier,
      double redirectedDamageMultiplier,
      double controlDurationMultiplier,
      double controlPotencyMultiplier
   ) {
      burstHealingMultiplier = valid(burstHealingMultiplier, "burstHealingMultiplier");
      periodicHealingMultiplier = valid(periodicHealingMultiplier, "periodicHealingMultiplier");
      groupedEnemyHealingMultiplier = valid(groupedEnemyHealingMultiplier, "groupedEnemyHealingMultiplier");
      periodicDurationMultiplier = valid(periodicDurationMultiplier, "periodicDurationMultiplier");
      shieldStrengthMultiplier = valid(shieldStrengthMultiplier, "shieldStrengthMultiplier");
      redirectedDamageMultiplier = valid(redirectedDamageMultiplier, "redirectedDamageMultiplier");
      controlDurationMultiplier = valid(controlDurationMultiplier, "controlDurationMultiplier");
      controlPotencyMultiplier = valid(controlPotencyMultiplier, "controlPotencyMultiplier");
      this.burstHealingMultiplier = burstHealingMultiplier;
      this.periodicHealingMultiplier = periodicHealingMultiplier;
      this.groupedEnemyHealingMultiplier = groupedEnemyHealingMultiplier;
      this.periodicDurationMultiplier = periodicDurationMultiplier;
      this.shieldStrengthMultiplier = shieldStrengthMultiplier;
      this.redirectedDamageMultiplier = redirectedDamageMultiplier;
      this.controlDurationMultiplier = controlDurationMultiplier;
      this.controlPotencyMultiplier = controlPotencyMultiplier;
   }

   private static double valid(double value, String name) {
      if (Double.isFinite(value) && !(value <= 0.0)) {
         return Math.max(0.25, Math.min(3.0, value));
      } else {
         throw new IllegalArgumentException(name + " must be finite and positive");
      }
   }
}

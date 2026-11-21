package com.magmaguy.elitemobs.advancedcombat.passives;

public record PassiveMechanics(
   double burstHealingMultiplier,
   double periodicHealingMultiplier,
   double groupedEnemyHealingMultiplier,
   double periodicDurationMultiplier,
   double shieldStrengthMultiplier,
   double redirectedDamageMultiplier,
   double controlDurationMultiplier,
   double controlPotencyMultiplier,
   double abilityCostMultiplier,
   double controlResistanceFraction,
   double partyMovementSpeedAdjustment
) {
   public static final PassiveMechanics NEUTRAL = new PassiveMechanics(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0);

   public PassiveMechanics(
      double burstHealingMultiplier,
      double periodicHealingMultiplier,
      double groupedEnemyHealingMultiplier,
      double periodicDurationMultiplier,
      double shieldStrengthMultiplier,
      double redirectedDamageMultiplier,
      double controlDurationMultiplier,
      double controlPotencyMultiplier,
      double abilityCostMultiplier,
      double controlResistanceFraction,
      double partyMovementSpeedAdjustment
   ) {
      burstHealingMultiplier = multiplier(burstHealingMultiplier, "burstHealingMultiplier");
      periodicHealingMultiplier = multiplier(periodicHealingMultiplier, "periodicHealingMultiplier");
      groupedEnemyHealingMultiplier = multiplier(groupedEnemyHealingMultiplier, "groupedEnemyHealingMultiplier");
      periodicDurationMultiplier = multiplier(periodicDurationMultiplier, "periodicDurationMultiplier");
      shieldStrengthMultiplier = multiplier(shieldStrengthMultiplier, "shieldStrengthMultiplier");
      redirectedDamageMultiplier = multiplier(redirectedDamageMultiplier, "redirectedDamageMultiplier");
      controlDurationMultiplier = multiplier(controlDurationMultiplier, "controlDurationMultiplier");
      controlPotencyMultiplier = multiplier(controlPotencyMultiplier, "controlPotencyMultiplier");
      abilityCostMultiplier = multiplier(abilityCostMultiplier, "abilityCostMultiplier");
      if (!Double.isFinite(controlResistanceFraction) || controlResistanceFraction < 0.0 || controlResistanceFraction >= 1.0) {
         throw new IllegalArgumentException("controlResistanceFraction must be in [0, 1)");
      } else if (!Double.isFinite(partyMovementSpeedAdjustment)) {
         throw new IllegalArgumentException("partyMovementSpeedAdjustment must be finite");
      } else {
         this.burstHealingMultiplier = burstHealingMultiplier;
         this.periodicHealingMultiplier = periodicHealingMultiplier;
         this.groupedEnemyHealingMultiplier = groupedEnemyHealingMultiplier;
         this.periodicDurationMultiplier = periodicDurationMultiplier;
         this.shieldStrengthMultiplier = shieldStrengthMultiplier;
         this.redirectedDamageMultiplier = redirectedDamageMultiplier;
         this.controlDurationMultiplier = controlDurationMultiplier;
         this.controlPotencyMultiplier = controlPotencyMultiplier;
         this.abilityCostMultiplier = abilityCostMultiplier;
         this.controlResistanceFraction = controlResistanceFraction;
         this.partyMovementSpeedAdjustment = partyMovementSpeedAdjustment;
      }
   }

   public PassiveMechanics(
      double burstHealingMultiplier,
      double periodicHealingMultiplier,
      double groupedEnemyHealingMultiplier,
      double periodicDurationMultiplier,
      double shieldStrengthMultiplier,
      double redirectedDamageMultiplier,
      double controlDurationMultiplier,
      double controlPotencyMultiplier,
      double controlResistanceFraction,
      double partyMovementSpeedAdjustment
   ) {
      this(
         burstHealingMultiplier,
         periodicHealingMultiplier,
         groupedEnemyHealingMultiplier,
         periodicDurationMultiplier,
         shieldStrengthMultiplier,
         redirectedDamageMultiplier,
         controlDurationMultiplier,
         controlPotencyMultiplier,
         1.0,
         controlResistanceFraction,
         partyMovementSpeedAdjustment
      );
   }

   public PassiveMechanics combine(PassiveMechanics other) {
      if (other == null) {
         throw new IllegalArgumentException("other must not be null");
      } else {
         return new PassiveMechanics(
            boundedMultiplier(this.burstHealingMultiplier * other.burstHealingMultiplier),
            boundedMultiplier(this.periodicHealingMultiplier * other.periodicHealingMultiplier),
            boundedMultiplier(this.groupedEnemyHealingMultiplier * other.groupedEnemyHealingMultiplier),
            boundedMultiplier(this.periodicDurationMultiplier * other.periodicDurationMultiplier),
            boundedMultiplier(this.shieldStrengthMultiplier * other.shieldStrengthMultiplier),
            boundedMultiplier(this.redirectedDamageMultiplier * other.redirectedDamageMultiplier),
            boundedMultiplier(this.controlDurationMultiplier * other.controlDurationMultiplier),
            boundedMultiplier(this.controlPotencyMultiplier * other.controlPotencyMultiplier),
            boundedMultiplier(this.abilityCostMultiplier * other.abilityCostMultiplier),
            Math.min(0.85, 1.0 - (1.0 - this.controlResistanceFraction) * (1.0 - other.controlResistanceFraction)),
            Math.max(-0.25, Math.min(0.25, this.partyMovementSpeedAdjustment + other.partyMovementSpeedAdjustment))
         );
      }
   }

   PassiveMechanics scaled(double factor) {
      if (Double.isFinite(factor) && !(factor < 0.0)) {
         return new PassiveMechanics(
            scaledMultiplier(this.burstHealingMultiplier, factor),
            scaledMultiplier(this.periodicHealingMultiplier, factor),
            scaledMultiplier(this.groupedEnemyHealingMultiplier, factor),
            scaledMultiplier(this.periodicDurationMultiplier, factor),
            scaledMultiplier(this.shieldStrengthMultiplier, factor),
            scaledMultiplier(this.redirectedDamageMultiplier, factor),
            scaledMultiplier(this.controlDurationMultiplier, factor),
            scaledMultiplier(this.controlPotencyMultiplier, factor),
            scaledMultiplier(this.abilityCostMultiplier, factor),
            Math.min(0.85, this.controlResistanceFraction * factor),
            Math.max(-0.25, Math.min(0.25, this.partyMovementSpeedAdjustment * factor))
         );
      } else {
         throw new IllegalArgumentException("factor must be finite and non-negative");
      }
   }

   private static double scaledMultiplier(double value, double factor) {
      return boundedMultiplier(1.0 + (value - 1.0) * factor);
   }

   private static double multiplier(double value, String name) {
      if (Double.isFinite(value) && !(value <= 0.0)) {
         return boundedMultiplier(value);
      } else {
         throw new IllegalArgumentException(name + " must be finite and positive");
      }
   }

   private static double boundedMultiplier(double value) {
      return Math.max(0.25, Math.min(3.0, value));
   }
}

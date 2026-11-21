package com.magmaguy.elitemobs.advancedcombat.abilities;

public record AbilityContribution(
   double damage, double effectiveHealing, double mitigatedDamage, double threat, int controlledEnemies, int supportedAllies, double movementDistance
) {
   public static final AbilityContribution NONE = new AbilityContribution(0.0, 0.0, 0.0, 0.0, 0, 0, 0.0);

   public AbilityContribution(
      double damage, double effectiveHealing, double mitigatedDamage, double threat, int controlledEnemies, int supportedAllies, double movementDistance
   ) {
      requireNonNegativeFinite(damage, "damage");
      requireNonNegativeFinite(effectiveHealing, "effectiveHealing");
      requireNonNegativeFinite(mitigatedDamage, "mitigatedDamage");
      requireNonNegativeFinite(threat, "threat");
      requireNonNegativeFinite(movementDistance, "movementDistance");
      if (controlledEnemies < 0) {
         throw new IllegalArgumentException("controlledEnemies must not be negative");
      } else if (supportedAllies < 0) {
         throw new IllegalArgumentException("supportedAllies must not be negative");
      } else {
         this.damage = damage;
         this.effectiveHealing = effectiveHealing;
         this.mitigatedDamage = mitigatedDamage;
         this.threat = threat;
         this.controlledEnemies = controlledEnemies;
         this.supportedAllies = supportedAllies;
         this.movementDistance = movementDistance;
      }
   }

   public AbilityContribution plus(AbilityContribution other) {
      return other == null
         ? this
         : new AbilityContribution(
            this.damage + other.damage,
            this.effectiveHealing + other.effectiveHealing,
            this.mitigatedDamage + other.mitigatedDamage,
            this.threat + other.threat,
            this.controlledEnemies + other.controlledEnemies,
            this.supportedAllies + other.supportedAllies,
            this.movementDistance + other.movementDistance
         );
   }

   public boolean isMeaningful() {
      return this.damage > 0.0 || this.effectiveHealing > 0.0 || this.mitigatedDamage > 0.0 || this.threat > 0.0;
   }

   private static void requireNonNegativeFinite(double value, String field) {
      if (!Double.isFinite(value) || value < 0.0) {
         throw new IllegalArgumentException(field + " must be finite and non-negative");
      }
   }
}

package com.magmaguy.elitemobs.advancedcombat.passives;

public record PassiveProfile(
   double outgoingDamageBase,
   double outgoingDamagePerLevel,
   double incomingDamageBase,
   double incomingDamagePerLevel,
   double movementSpeedBase,
   double movementSpeedPerLevel,
   double healingDoneBase,
   double healingDonePerLevel,
   double abilityCostReductionBase,
   double abilityCostReductionPerLevel,
   double healingReceivedBase,
   double healingReceivedPerLevel
) {
   public PassiveProfile(
      double outgoingDamageBase,
      double outgoingDamagePerLevel,
      double incomingDamageBase,
      double incomingDamagePerLevel,
      double movementSpeedBase,
      double movementSpeedPerLevel,
      double healingDoneBase,
      double healingDonePerLevel,
      double abilityCostReductionBase,
      double abilityCostReductionPerLevel,
      double healingReceivedBase,
      double healingReceivedPerLevel
   ) {
      requireFinite(outgoingDamageBase, "outgoingDamageBase");
      requireFinite(outgoingDamagePerLevel, "outgoingDamagePerLevel");
      requireFinite(incomingDamageBase, "incomingDamageBase");
      requireFinite(incomingDamagePerLevel, "incomingDamagePerLevel");
      requireFinite(movementSpeedBase, "movementSpeedBase");
      requireFinite(movementSpeedPerLevel, "movementSpeedPerLevel");
      requireFinite(healingDoneBase, "healingDoneBase");
      requireFinite(healingDonePerLevel, "healingDonePerLevel");
      requireFinite(abilityCostReductionBase, "abilityCostReductionBase");
      requireFinite(abilityCostReductionPerLevel, "abilityCostReductionPerLevel");
      requireFinite(healingReceivedBase, "healingReceivedBase");
      requireFinite(healingReceivedPerLevel, "healingReceivedPerLevel");
      this.outgoingDamageBase = outgoingDamageBase;
      this.outgoingDamagePerLevel = outgoingDamagePerLevel;
      this.incomingDamageBase = incomingDamageBase;
      this.incomingDamagePerLevel = incomingDamagePerLevel;
      this.movementSpeedBase = movementSpeedBase;
      this.movementSpeedPerLevel = movementSpeedPerLevel;
      this.healingDoneBase = healingDoneBase;
      this.healingDonePerLevel = healingDonePerLevel;
      this.abilityCostReductionBase = abilityCostReductionBase;
      this.abilityCostReductionPerLevel = abilityCostReductionPerLevel;
      this.healingReceivedBase = healingReceivedBase;
      this.healingReceivedPerLevel = healingReceivedPerLevel;
   }

   public PassiveProfile.Contribution atContributionLevel(int level) {
      if (level < 1) {
         throw new IllegalArgumentException("Passive contribution level must be positive");
      } else {
         return new PassiveProfile.Contribution(
            this.outgoingDamageBase + this.outgoingDamagePerLevel * level,
            this.incomingDamageBase + this.incomingDamagePerLevel * level,
            this.movementSpeedBase + this.movementSpeedPerLevel * level,
            this.healingDoneBase + this.healingDonePerLevel * level,
            this.abilityCostReductionBase + this.abilityCostReductionPerLevel * level,
            this.healingReceivedBase + this.healingReceivedPerLevel * level
         );
      }
   }

   private static void requireFinite(double value, String name) {
      if (!Double.isFinite(value)) {
         throw new IllegalArgumentException(name + " must be finite");
      }
   }

   public record Contribution(
      double outgoingDamage, double incomingDamage, double movementSpeed, double healingDone, double abilityCostReduction, double healingReceived
   ) {
      public PassiveProfile.Contribution plus(PassiveProfile.Contribution other) {
         return new PassiveProfile.Contribution(
            this.outgoingDamage + other.outgoingDamage,
            this.incomingDamage + other.incomingDamage,
            this.movementSpeed + other.movementSpeed,
            this.healingDone + other.healingDone,
            this.abilityCostReduction + other.abilityCostReduction,
            this.healingReceived + other.healingReceived
         );
      }
   }
}

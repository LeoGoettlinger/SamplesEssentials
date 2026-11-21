package com.magmaguy.elitemobs.advancedcombat.presentation;

public record AbilityPresentationBudget(int particleLimit, int soundLimit, int targetBurstLimit, int traceSampleLimit) {
   public static final AbilityPresentationBudget STANDARD = new AbilityPresentationBudget(160, 8, 8, 18);

   public AbilityPresentationBudget(int particleLimit, int soundLimit, int targetBurstLimit, int traceSampleLimit) {
      if (particleLimit >= 1 && soundLimit >= 1 && targetBurstLimit >= 1 && traceSampleLimit >= 1) {
         this.particleLimit = particleLimit;
         this.soundLimit = soundLimit;
         this.targetBurstLimit = targetBurstLimit;
         this.traceSampleLimit = traceSampleLimit;
      } else {
         throw new IllegalArgumentException("Presentation limits must be positive");
      }
   }
}

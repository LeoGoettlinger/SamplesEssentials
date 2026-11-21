package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;

public record AbilityResult(
   boolean successful, String abilityId, AbilityFailureReason failureReason, AbilityContribution contribution, boolean continuesAsynchronously
) {
   public AbilityResult(
      boolean successful, String abilityId, AbilityFailureReason failureReason, AbilityContribution contribution, boolean continuesAsynchronously
   ) {
      Objects.requireNonNull(abilityId, "abilityId");
      failureReason = Objects.requireNonNull(failureReason, "failureReason");
      contribution = Objects.requireNonNull(contribution, "contribution");
      if (successful != (failureReason == AbilityFailureReason.NONE)) {
         throw new IllegalArgumentException("Success and failure reason disagree");
      } else {
         this.successful = successful;
         this.abilityId = abilityId;
         this.failureReason = failureReason;
         this.contribution = contribution;
         this.continuesAsynchronously = continuesAsynchronously;
      }
   }

   public static AbilityResult success(String abilityId, AbilityContribution contribution) {
      return new AbilityResult(true, abilityId, AbilityFailureReason.NONE, contribution, false);
   }

   public static AbilityResult scheduled(String abilityId, AbilityContribution contribution) {
      return new AbilityResult(true, abilityId, AbilityFailureReason.NONE, contribution, true);
   }

   public static AbilityResult failure(String abilityId, AbilityFailureReason reason) {
      if (reason == AbilityFailureReason.NONE) {
         throw new IllegalArgumentException("Failure requires a reason");
      } else {
         return new AbilityResult(false, abilityId, reason, AbilityContribution.NONE, false);
      }
   }
}

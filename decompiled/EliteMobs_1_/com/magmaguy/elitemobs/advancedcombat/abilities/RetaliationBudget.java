package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;
import java.util.UUID;

record RetaliationBudget(double amount, String abilityId, UUID lastAttackerId) {
   RetaliationBudget(double amount, String abilityId, UUID lastAttackerId) {
      if (Double.isFinite(amount) && !(amount < 0.0)) {
         Objects.requireNonNull(abilityId, "abilityId");
         Objects.requireNonNull(lastAttackerId, "lastAttackerId");
         this.amount = amount;
         this.abilityId = abilityId;
         this.lastAttackerId = lastAttackerId;
      } else {
         throw new IllegalArgumentException("amount must be finite and non-negative");
      }
   }

   static RetaliationBudget accumulate(RetaliationBudget current, double gained, double cap, String abilityId, UUID attackerId) {
      Objects.requireNonNull(abilityId, "abilityId");
      Objects.requireNonNull(attackerId, "attackerId");
      double boundedGain = Double.isFinite(gained) ? Math.max(0.0, gained) : 0.0;
      double boundedCap = Double.isFinite(cap) ? Math.max(0.0, cap) : 0.0;
      double existing = current != null && current.abilityId().equals(abilityId) ? current.amount() : 0.0;
      return new RetaliationBudget(Math.min(boundedCap, existing + boundedGain), abilityId, attackerId);
   }
}

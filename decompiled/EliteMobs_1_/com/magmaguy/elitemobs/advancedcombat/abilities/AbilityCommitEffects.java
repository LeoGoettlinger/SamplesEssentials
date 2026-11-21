package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Set;

public record AbilityCommitEffects(double resourceGrant) {
   private static final double RESOURCE_BURST_BASE = 40.0;
   private static final double RESOURCE_BURST_PER_LEVEL = 0.05;
   public static final AbilityCommitEffects NONE = new AbilityCommitEffects(0.0);

   public AbilityCommitEffects(double resourceGrant) {
      if (Double.isFinite(resourceGrant) && !(resourceGrant < 0.0)) {
         this.resourceGrant = resourceGrant;
      } else {
         throw new IllegalArgumentException("resourceGrant must be finite and non-negative");
      }
   }

   public static AbilityCommitEffects resolve(FixedAbilitySpec spec, int effectiveLevel) {
      Set<AbilityMechanic> mechanics = spec.executionTraits().mechanics();
      double resource = mechanics.contains(AbilityMechanic.RESOURCE_BURST) ? 40.0 + Math.max(0, effectiveLevel) * 0.05 : 0.0;
      return resource == 0.0 ? NONE : new AbilityCommitEffects(resource);
   }
}

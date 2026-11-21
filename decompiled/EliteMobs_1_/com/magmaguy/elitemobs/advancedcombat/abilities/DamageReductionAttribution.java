package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;
import java.util.UUID;

record DamageReductionAttribution(UUID sourceId, UUID targetId, String abilityId, double damageAfterReduction, double preventedBeforeDownstream) {
   DamageReductionAttribution(UUID sourceId, UUID targetId, String abilityId, double damageAfterReduction, double preventedBeforeDownstream) {
      Objects.requireNonNull(sourceId, "sourceId");
      Objects.requireNonNull(targetId, "targetId");
      Objects.requireNonNull(abilityId, "abilityId");
      if (!Double.isFinite(damageAfterReduction) || damageAfterReduction < 0.0) {
         throw new IllegalArgumentException("damageAfterReduction must be finite and non-negative");
      } else if (Double.isFinite(preventedBeforeDownstream) && !(preventedBeforeDownstream < 0.0)) {
         this.sourceId = sourceId;
         this.targetId = targetId;
         this.abilityId = abilityId;
         this.damageAfterReduction = damageAfterReduction;
         this.preventedBeforeDownstream = preventedBeforeDownstream;
      } else {
         throw new IllegalArgumentException("preventedBeforeDownstream must be finite and non-negative");
      }
   }

   double survivingPreventedDamage(double finalDamage, boolean cancelled) {
      if (!cancelled
         && Double.isFinite(finalDamage)
         && !(finalDamage <= 0.0)
         && !(this.damageAfterReduction <= 0.0)
         && !(this.preventedBeforeDownstream <= 0.0)) {
         double downstreamRatio = finalDamage / this.damageAfterReduction;
         double surviving = this.preventedBeforeDownstream * downstreamRatio;
         return Double.isFinite(surviving) && surviving > 0.0 ? surviving : 0.0;
      } else {
         return 0.0;
      }
   }
}

package com.magmaguy.elitemobs.advancedcombat.presentation;

import java.util.Objects;

public final class AbilityPresentationBudgetLedger {
   private final AbilityPresentationPlan plan;
   private int remainingParticles;
   private int remainingSounds;
   private int remainingTargetBursts;

   AbilityPresentationBudgetLedger(AbilityPresentationPlan plan) {
      this.plan = Objects.requireNonNull(plan, "plan");
      this.remainingParticles = plan.budget().particleLimit();
      this.remainingSounds = plan.budget().soundLimit();
      this.remainingTargetBursts = plan.budget().targetBurstLimit();
   }

   public AbilityPresentationBudgetLedger.Grant claim(AbilityPresentationCue cue, int desiredParticles, int desiredSounds) {
      Objects.requireNonNull(cue, "cue");
      if (desiredParticles >= 0 && desiredSounds >= 0) {
         if (!this.plan.cues().contains(cue)) {
            return AbilityPresentationBudgetLedger.Grant.NONE;
         } else if (cue.targetScoped() && this.remainingTargetBursts == 0) {
            return AbilityPresentationBudgetLedger.Grant.NONE;
         } else {
            int particles = Math.min(desiredParticles, this.remainingParticles);
            int sounds = Math.min(desiredSounds, this.remainingSounds);
            if (particles == 0 && sounds == 0) {
               return AbilityPresentationBudgetLedger.Grant.NONE;
            } else {
               this.remainingParticles -= particles;
               this.remainingSounds -= sounds;
               if (cue.targetScoped()) {
                  this.remainingTargetBursts--;
               }

               return new AbilityPresentationBudgetLedger.Grant(particles, sounds);
            }
         }
      } else {
         throw new IllegalArgumentException("Requested presentation work must not be negative");
      }
   }

   boolean canRender(AbilityPresentationCue cue) {
      return this.plan.cues().contains(cue)
         && (!cue.targetScoped() || this.remainingTargetBursts > 0)
         && (this.remainingParticles > 0 || this.remainingSounds > 0);
   }

   public record Grant(int particles, int sounds) {
      public static final AbilityPresentationBudgetLedger.Grant NONE = new AbilityPresentationBudgetLedger.Grant(0, 0);

      public Grant(int particles, int sounds) {
         if (particles >= 0 && sounds >= 0) {
            this.particles = particles;
            this.sounds = sounds;
         } else {
            throw new IllegalArgumentException("Granted presentation work must not be negative");
         }
      }

      public boolean granted() {
         return this.particles > 0 || this.sounds > 0;
      }
   }
}

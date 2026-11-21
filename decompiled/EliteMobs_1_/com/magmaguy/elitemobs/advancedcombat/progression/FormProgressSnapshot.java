package com.magmaguy.elitemobs.advancedcombat.progression;

import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import java.util.Objects;

public record FormProgressSnapshot(
   String formId,
   boolean unlocked,
   List<UnlockBlocker> unlockBlockers,
   long xp,
   int localLevel,
   int effectiveLevel,
   int localCap,
   int effectiveCap,
   long xpAtCap,
   ProgressionCapReason capReason,
   List<SkillType> limitingSkills
) {
   public FormProgressSnapshot(
      String formId,
      boolean unlocked,
      List<UnlockBlocker> unlockBlockers,
      long xp,
      int localLevel,
      int effectiveLevel,
      int localCap,
      int effectiveCap,
      long xpAtCap,
      ProgressionCapReason capReason,
      List<SkillType> limitingSkills
   ) {
      Objects.requireNonNull(formId, "formId");
      unlockBlockers = List.copyOf(unlockBlockers);
      if (unlocked != unlockBlockers.isEmpty()) {
         throw new IllegalArgumentException("Unlocked state must match the blocker list");
      } else {
         Objects.requireNonNull(capReason, "capReason");
         limitingSkills = List.copyOf(limitingSkills);
         if (unlocked
            || xp == 0L
               && localLevel == 0
               && effectiveLevel == 0
               && localCap == 0
               && effectiveCap == 0
               && xpAtCap == 0L
               && capReason == ProgressionCapReason.NOT_APPLICABLE
               && limitingSkills.isEmpty()) {
            if (!unlocked || localLevel >= 1 && effectiveLevel >= 1 && localLevel <= localCap && effectiveLevel <= effectiveCap) {
               if (capReason == ProgressionCapReason.BAND_COMPLETE && !limitingSkills.isEmpty()) {
                  throw new IllegalArgumentException("A completed band has no limiting foundation skill");
               } else {
                  this.formId = formId;
                  this.unlocked = unlocked;
                  this.unlockBlockers = unlockBlockers;
                  this.xp = xp;
                  this.localLevel = localLevel;
                  this.effectiveLevel = effectiveLevel;
                  this.localCap = localCap;
                  this.effectiveCap = effectiveCap;
                  this.xpAtCap = xpAtCap;
                  this.capReason = capReason;
                  this.limitingSkills = limitingSkills;
               }
            } else {
               throw new IllegalArgumentException("Unlocked form level must fit within its visible cap");
            }
         } else {
            throw new IllegalArgumentException("Locked forms must not expose progression levels or caps");
         }
      }
   }
}

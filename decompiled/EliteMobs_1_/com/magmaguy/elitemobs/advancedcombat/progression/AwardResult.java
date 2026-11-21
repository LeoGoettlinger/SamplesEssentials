package com.magmaguy.elitemobs.advancedcombat.progression;

import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;
import java.util.Objects;

public record AwardResult(
   AwardResult.Status status,
   String formId,
   long requestedXp,
   long appliedXp,
   long discardedXp,
   long previousXp,
   long currentXp,
   int previousLocalLevel,
   int currentLocalLevel,
   int currentEffectiveLevel,
   int localCap,
   int effectiveCap,
   ProgressionCapReason capReason,
   List<SkillType> limitingSkills
) {
   public AwardResult(
      AwardResult.Status status,
      String formId,
      long requestedXp,
      long appliedXp,
      long discardedXp,
      long previousXp,
      long currentXp,
      int previousLocalLevel,
      int currentLocalLevel,
      int currentEffectiveLevel,
      int localCap,
      int effectiveCap,
      ProgressionCapReason capReason,
      List<SkillType> limitingSkills
   ) {
      capReason = Objects.requireNonNull(capReason, "capReason");
      limitingSkills = List.copyOf(limitingSkills);
      if (capReason == ProgressionCapReason.BAND_COMPLETE && !limitingSkills.isEmpty()) {
         throw new IllegalArgumentException("A completed band has no limiting foundation skill");
      } else if (capReason == ProgressionCapReason.NOT_APPLICABLE && !limitingSkills.isEmpty()) {
         throw new IllegalArgumentException("An outcome without a cap cannot have limiting skills");
      } else {
         this.status = status;
         this.formId = formId;
         this.requestedXp = requestedXp;
         this.appliedXp = appliedXp;
         this.discardedXp = discardedXp;
         this.previousXp = previousXp;
         this.currentXp = currentXp;
         this.previousLocalLevel = previousLocalLevel;
         this.currentLocalLevel = currentLocalLevel;
         this.currentEffectiveLevel = currentEffectiveLevel;
         this.localCap = localCap;
         this.effectiveCap = effectiveCap;
         this.capReason = capReason;
         this.limitingSkills = limitingSkills;
      }
   }

   public AwardResult(
      AwardResult.Status status,
      String formId,
      long requestedXp,
      long appliedXp,
      long discardedXp,
      long previousXp,
      long currentXp,
      int previousLocalLevel,
      int currentLocalLevel,
      int currentEffectiveLevel,
      int localCap,
      int effectiveCap,
      List<SkillType> limitingSkills
   ) {
      this(
         status,
         formId,
         requestedXp,
         appliedXp,
         discardedXp,
         previousXp,
         currentXp,
         previousLocalLevel,
         currentLocalLevel,
         currentEffectiveLevel,
         localCap,
         effectiveCap,
         ProgressionCapReason.NOT_APPLICABLE,
         limitingSkills
      );
   }

   public boolean changed() {
      return this.appliedXp > 0L;
   }

   public static enum Status {
      APPLIED,
      PARTIALLY_APPLIED,
      AT_CAP,
      INVALID_AMOUNT,
      NOT_READY,
      NO_SELECTED_FORM,
      FORM_LOCKED;
   }
}

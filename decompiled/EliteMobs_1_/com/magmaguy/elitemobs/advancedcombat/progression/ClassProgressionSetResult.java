package com.magmaguy.elitemobs.advancedcombat.progression;

import com.magmaguy.elitemobs.skills.SkillType;
import java.util.List;

public record ClassProgressionSetResult(
   ClassProgressionSetResult.Status status,
   String formId,
   int requestedEffectiveLevel,
   String blockingFormId,
   int effectiveCap,
   List<SkillType> limitingSkills,
   ProfileSnapshot snapshot
) {
   public ClassProgressionSetResult(
      ClassProgressionSetResult.Status status,
      String formId,
      int requestedEffectiveLevel,
      String blockingFormId,
      int effectiveCap,
      List<SkillType> limitingSkills,
      ProfileSnapshot snapshot
   ) {
      limitingSkills = List.copyOf(limitingSkills);
      this.status = status;
      this.formId = formId;
      this.requestedEffectiveLevel = requestedEffectiveLevel;
      this.blockingFormId = blockingFormId;
      this.effectiveCap = effectiveCap;
      this.limitingSkills = limitingSkills;
      this.snapshot = snapshot;
   }

   public boolean applied() {
      return this.status == ClassProgressionSetResult.Status.APPLIED;
   }

   public static enum Status {
      APPLIED,
      NOT_READY,
      UNKNOWN_FORM,
      LEVEL_OUTSIDE_FORM_BAND,
      FOUNDATION_SKILL_CAP,
      RUN_LOCKED;
   }
}

package com.magmaguy.elitemobs.advancedcombat.progression;

import com.magmaguy.elitemobs.skills.SkillType;
import java.util.Objects;
import java.util.Optional;

public record UnlockBlocker(UnlockBlocker.Kind kind, String formId, SkillType skillType, int currentLevel, int requiredLevel, String reason) {
   public UnlockBlocker(UnlockBlocker.Kind kind, String formId, SkillType skillType, int currentLevel, int requiredLevel, String reason) {
      Objects.requireNonNull(kind, "kind");
      Objects.requireNonNull(formId, "formId");
      if (formId.isBlank()) {
         throw new IllegalArgumentException("formId must not be blank");
      } else if (currentLevel < 0) {
         throw new IllegalArgumentException("currentLevel must not be negative");
      } else {
         boolean explained = kind == UnlockBlocker.Kind.CONTENT_REQUIREMENT || kind == UnlockBlocker.Kind.CLASS_CHALLENGE;
         if (!explained && requiredLevel < 1) {
            throw new IllegalArgumentException("requiredLevel must be positive");
         } else if (kind == UnlockBlocker.Kind.FOUNDATION_SKILL != (skillType != null)) {
            throw new IllegalArgumentException("Only foundation-skill blockers have a skill type");
         } else if (explained != (reason != null)) {
            throw new IllegalArgumentException("Only content blockers have a reason");
         } else if (reason != null && reason.isBlank()) {
            throw new IllegalArgumentException("Content blocker reason must not be blank");
         } else {
            this.kind = kind;
            this.formId = formId;
            this.skillType = skillType;
            this.currentLevel = currentLevel;
            this.requiredLevel = requiredLevel;
            this.reason = reason;
         }
      }
   }

   public static UnlockBlocker foundationSkill(String formId, SkillType skillType, int currentLevel, int requiredLevel) {
      return new UnlockBlocker(UnlockBlocker.Kind.FOUNDATION_SKILL, formId, Objects.requireNonNull(skillType, "skillType"), currentLevel, requiredLevel, null);
   }

   public static UnlockBlocker parentLocalLevel(String parentFormId, int currentLevel, int requiredLevel) {
      return new UnlockBlocker(UnlockBlocker.Kind.PARENT_LOCAL_LEVEL, parentFormId, null, currentLevel, requiredLevel, null);
   }

   public static UnlockBlocker contentRequirement(String rootFormId, String reason) {
      return new UnlockBlocker(UnlockBlocker.Kind.CONTENT_REQUIREMENT, rootFormId, null, 0, 0, Objects.requireNonNull(reason, "reason"));
   }

   public static UnlockBlocker classChallenge(String formId) {
      return new UnlockBlocker(UnlockBlocker.Kind.CLASS_CHALLENGE, formId, null, 0, 0, "Defeat this class's instructor in a solo trial.");
   }

   public Optional<SkillType> optionalSkillType() {
      return Optional.ofNullable(this.skillType);
   }

   public Optional<String> optionalReason() {
      return Optional.ofNullable(this.reason);
   }

   public static enum Kind {
      FOUNDATION_SKILL,
      PARENT_LOCAL_LEVEL,
      CONTENT_REQUIREMENT,
      CLASS_CHALLENGE;
   }
}

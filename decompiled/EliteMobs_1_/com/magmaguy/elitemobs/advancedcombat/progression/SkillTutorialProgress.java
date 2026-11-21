package com.magmaguy.elitemobs.advancedcombat.progression;

import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;

public record SkillTutorialProgress(int usedSkills) {
   public SkillTutorialProgress(int usedSkills) {
      usedSkills &= 7;
      this.usedSkills = usedSkills;
   }

   public boolean complete() {
      return this.usedSkills == 7;
   }

   public boolean hasUsed(AbilitySlot slot) {
      return (this.usedSkills & bit(slot)) != 0;
   }

   public SkillTutorialProgress withUsed(AbilitySlot slot) {
      return new SkillTutorialProgress(this.usedSkills | bit(slot));
   }

   private static int bit(AbilitySlot slot) {
      return switch (slot) {
         case MOBILITY -> 1;
         case SIGNATURE -> 2;
         case UTILITY -> 4;
      };
   }
}

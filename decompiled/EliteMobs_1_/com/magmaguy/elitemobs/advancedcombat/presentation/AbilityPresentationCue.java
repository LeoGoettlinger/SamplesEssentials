package com.magmaguy.elitemobs.advancedcombat.presentation;

public enum AbilityPresentationCue {
   CAST(false),
   IMPACT(true),
   HEAL(true),
   BUFF(true),
   CONTROL(true),
   MOBILITY(false);

   private final boolean targetScoped;

   private AbilityPresentationCue(boolean targetScoped) {
      this.targetScoped = targetScoped;
   }

   boolean targetScoped() {
      return this.targetScoped;
   }
}

package com.magmaguy.elitemobs.advancedcombat.presentation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record AbilityPresentationPlan(AbilityPresentationTheme theme, Set<AbilityPresentationCue> cues, AbilityPresentationBudget budget) {
   public AbilityPresentationPlan(AbilityPresentationTheme theme, Set<AbilityPresentationCue> cues, AbilityPresentationBudget budget) {
      theme = Objects.requireNonNull(theme, "theme");
      cues = Set.copyOf(cues);
      budget = Objects.requireNonNull(budget, "budget");
      if (!cues.contains(AbilityPresentationCue.CAST)) {
         throw new IllegalArgumentException("Every presentation plan needs a cast cue");
      } else {
         this.theme = theme;
         this.cues = cues;
         this.budget = budget;
      }
   }

   public AbilityPresentationBudgetLedger newBudgetLedger() {
      return new AbilityPresentationBudgetLedger(this);
   }

   public List<Double> traceProgress(double distance) {
      if (!Double.isFinite(distance)) {
         throw new IllegalArgumentException("Trace distance must be finite");
      } else if (distance <= 0.0) {
         return List.of();
      } else {
         int count = Math.min(this.budget.traceSampleLimit(), Math.max(1, (int)Math.ceil(distance * 2.0)));
         List<Double> samples = new ArrayList<>(count);

         for (int index = 1; index <= count; index++) {
            samples.add((double)index / (count + 1));
         }

         return List.copyOf(samples);
      }
   }
}

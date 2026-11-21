package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public record AbilityExecutionTraits(boolean establishesMobilityAnchor, double selfHealingMultiplier, Set<AbilityMechanic> mechanics) {
   public static final AbilityExecutionTraits STANDARD = new AbilityExecutionTraits(false, 1.0, Set.of());

   public AbilityExecutionTraits(boolean establishesMobilityAnchor, double selfHealingMultiplier, Set<AbilityMechanic> mechanics) {
      if (Double.isFinite(selfHealingMultiplier) && !(selfHealingMultiplier < 0.0)) {
         mechanics = Set.copyOf(mechanics);
         this.establishesMobilityAnchor = establishesMobilityAnchor;
         this.selfHealingMultiplier = selfHealingMultiplier;
         this.mechanics = mechanics;
      } else {
         throw new IllegalArgumentException("selfHealingMultiplier must be finite and non-negative");
      }
   }

   public static AbilityExecutionTraits mobilityAnchor() {
      return new AbilityExecutionTraits(true, 1.0, Set.of());
   }

   public static AbilityExecutionTraits reducedSelfHealing(double multiplier) {
      return new AbilityExecutionTraits(false, multiplier, Set.of());
   }

   public static AbilityExecutionTraits mechanics(AbilityMechanic... mechanics) {
      return new AbilityExecutionTraits(false, 1.0, (Set<AbilityMechanic>)(mechanics.length == 0 ? Set.of() : EnumSet.of(mechanics[0], mechanics)));
   }

   public static AbilityExecutionTraits mobilityAnchor(AbilityMechanic... mechanics) {
      return new AbilityExecutionTraits(true, 1.0, (Set<AbilityMechanic>)(mechanics.length == 0 ? Set.of() : EnumSet.of(mechanics[0], mechanics)));
   }

   public AbilityExecutionTraits plus(AbilityMechanic... additional) {
      if (additional.length == 0) {
         return this;
      } else {
         EnumSet<AbilityMechanic> combined = this.mechanics.isEmpty() ? EnumSet.noneOf(AbilityMechanic.class) : EnumSet.copyOf(this.mechanics);
         Collections.addAll(combined, additional);
         return new AbilityExecutionTraits(this.establishesMobilityAnchor, this.selfHealingMultiplier, combined);
      }
   }
}

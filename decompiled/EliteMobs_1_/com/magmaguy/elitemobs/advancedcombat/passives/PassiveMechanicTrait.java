package com.magmaguy.elitemobs.advancedcombat.passives;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public record PassiveMechanicTrait(Set<PassiveCondition> conditions, PassiveMechanics authoredAtLevelThirty) {
   public PassiveMechanicTrait(Set<PassiveCondition> conditions, PassiveMechanics authoredAtLevelThirty) {
      Objects.requireNonNull(conditions, "conditions");
      Objects.requireNonNull(authoredAtLevelThirty, "authoredAtLevelThirty");
      conditions = conditions.isEmpty() ? Set.of(PassiveCondition.ALWAYS) : Set.copyOf(EnumSet.copyOf(conditions));
      this.conditions = conditions;
      this.authoredAtLevelThirty = authoredAtLevelThirty;
   }

   public boolean unconditional() {
      return this.conditions.size() == 1 && this.conditions.contains(PassiveCondition.ALWAYS);
   }

   public PassiveMechanics atContributionLevel(int level) {
      if (level < 1) {
         throw new IllegalArgumentException("Passive contribution level must be positive");
      } else {
         return this.authoredAtLevelThirty.scaled(0.7 + level * 0.01);
      }
   }
}

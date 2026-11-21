package com.magmaguy.elitemobs.advancedcombat.passives;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public record PassiveTrait(Set<PassiveCondition> conditions, PassiveProfile profile) {
   public PassiveTrait(Set<PassiveCondition> conditions, PassiveProfile profile) {
      Objects.requireNonNull(conditions, "conditions");
      Objects.requireNonNull(profile, "profile");
      conditions = conditions.isEmpty() ? Set.of(PassiveCondition.ALWAYS) : Set.copyOf(EnumSet.copyOf(conditions));
      this.conditions = conditions;
      this.profile = profile;
   }

   public boolean unconditional() {
      return this.conditions.size() == 1 && this.conditions.contains(PassiveCondition.ALWAYS);
   }

   public boolean matches(PassiveConditionContext context) {
      Objects.requireNonNull(context, "context");
      return this.conditions.stream().allMatch(condition -> condition.matches(context));
   }
}

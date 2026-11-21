package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.classes.AbilitySlot;
import java.util.Objects;
import java.util.Set;

public record FixedAbilitySpec(
   String id,
   AbilitySlot slot,
   AbilityFamily family,
   AbilityTarget target,
   Set<AbilityEffect> effects,
   AbilityTuning tuning,
   AbilityExecutionTraits executionTraits,
   double resourceCost
) {
   public FixedAbilitySpec(
      String id,
      AbilitySlot slot,
      AbilityFamily family,
      AbilityTarget target,
      Set<AbilityEffect> effects,
      AbilityTuning tuning,
      AbilityExecutionTraits executionTraits,
      double resourceCost
   ) {
      Objects.requireNonNull(id, "id");
      slot = Objects.requireNonNull(slot, "slot");
      family = Objects.requireNonNull(family, "family");
      target = Objects.requireNonNull(target, "target");
      effects = Set.copyOf(effects);
      if (effects.isEmpty() && family == AbilityFamily.INSTANT) {
         throw new IllegalArgumentException("Instant ability needs an effect: " + id);
      } else {
         tuning = Objects.requireNonNull(tuning, "tuning");
         executionTraits = Objects.requireNonNull(executionTraits, "executionTraits");
         if (Double.isFinite(resourceCost) && !(resourceCost <= 0.0) && !(resourceCost > 100.0)) {
            this.id = id;
            this.slot = slot;
            this.family = family;
            this.target = target;
            this.effects = effects;
            this.tuning = tuning;
            this.executionTraits = executionTraits;
            this.resourceCost = resourceCost;
         } else {
            throw new IllegalArgumentException("resourceCost must be within (0, 100]: " + id);
         }
      }
   }

   FixedAbilitySpec withMechanics(AbilityMechanic... mechanics) {
      return new FixedAbilitySpec(
         this.id, this.slot, this.family, this.target, this.effects, this.tuning, this.executionTraits.plus(mechanics), this.resourceCost
      );
   }

   FixedAbilitySpec withResourceCost(double amount) {
      return new FixedAbilitySpec(this.id, this.slot, this.family, this.target, this.effects, this.tuning, this.executionTraits, amount);
   }

   FixedAbilitySpec withWeakenMultiplier(double multiplier) {
      return new FixedAbilitySpec(
         this.id, this.slot, this.family, this.target, this.effects, this.tuning.withWeakenMultiplier(multiplier), this.executionTraits, this.resourceCost
      );
   }
}

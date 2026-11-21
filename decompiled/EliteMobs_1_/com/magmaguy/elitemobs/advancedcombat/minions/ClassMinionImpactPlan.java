package com.magmaguy.elitemobs.advancedcombat.minions;

import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityEffect;
import com.magmaguy.elitemobs.advancedcombat.abilities.AbilityFamily;
import com.magmaguy.elitemobs.advancedcombat.abilities.FixedAbilitySpec;
import java.util.EnumSet;
import java.util.Set;

public record ClassMinionImpactPlan(double lifestealFraction, Set<AbilityEffect> controlEffects) {
   private static final double STANDARD_LIFESTEAL_FRACTION = 0.14;
   private static final Set<AbilityEffect> SUPPORTED_CONTROL_EFFECTS = EnumSet.of(
      AbilityEffect.SLOW, AbilityEffect.WEAKEN, AbilityEffect.INTERRUPT, AbilityEffect.FEAR
   );

   public ClassMinionImpactPlan(double lifestealFraction, Set<AbilityEffect> controlEffects) {
      if (Double.isFinite(lifestealFraction) && !(lifestealFraction < 0.0) && !(lifestealFraction > 1.0)) {
         controlEffects = Set.copyOf(controlEffects);
         this.lifestealFraction = lifestealFraction;
         this.controlEffects = controlEffects;
      } else {
         throw new IllegalArgumentException("lifestealFraction must be within [0, 1]");
      }
   }

   public static ClassMinionImpactPlan from(FixedAbilitySpec spec) {
      if (spec.family() != AbilityFamily.SUMMON) {
         throw new IllegalArgumentException("Only summon abilities have servant impacts: " + spec.id());
      } else {
         EnumSet<AbilityEffect> controls = EnumSet.noneOf(AbilityEffect.class);
         controls.addAll(spec.effects());
         controls.retainAll(SUPPORTED_CONTROL_EFFECTS);
         return new ClassMinionImpactPlan(spec.effects().contains(AbilityEffect.LIFESTEAL) ? 0.14 : 0.0, controls);
      }
   }
}

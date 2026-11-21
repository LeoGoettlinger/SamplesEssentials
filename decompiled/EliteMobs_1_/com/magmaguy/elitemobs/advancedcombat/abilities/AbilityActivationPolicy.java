package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Set;

final class AbilityActivationPolicy {
   static final double LOW_HEALTH_ALLY_FRACTION = 0.35;

   private AbilityActivationPolicy() {
   }

   static boolean permits(
      Set<AbilityMechanic> mechanics, boolean casterMoving, double nearestEnemyDistance, double minimumRange, double lowestAllyHealthFraction
   ) {
      if (mechanics.contains(AbilityMechanic.REQUIRES_MOVEMENT) && !casterMoving) {
         return false;
      } else {
         return mechanics.contains(AbilityMechanic.MINIMUM_RANGE) && nearestEnemyDistance < minimumRange
            ? false
            : !mechanics.contains(AbilityMechanic.LOW_HEALTH_ALLY_ONLY) || lowestAllyHealthFraction <= 0.35;
      }
   }

   static boolean permitsImpact(Set<AbilityMechanic> mechanics, double originToImpactDistance, double originToTargetDistance, double minimumRange) {
      return !mechanics.contains(AbilityMechanic.MINIMUM_RANGE)
         ? true
         : Double.isFinite(originToImpactDistance)
            && Double.isFinite(originToTargetDistance)
            && originToImpactDistance >= minimumRange
            && originToTargetDistance >= minimumRange;
   }
}

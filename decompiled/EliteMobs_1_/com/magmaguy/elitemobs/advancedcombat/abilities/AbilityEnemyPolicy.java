package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Set;

final class AbilityEnemyPolicy {
   static final double WOUNDED_HEALTH_FRACTION = 0.5;

   private AbilityEnemyPolicy() {
   }

   static boolean permits(Set<AbilityMechanic> mechanics, AbilityEnemyPolicy.EnemyFacts target) {
      if (mechanics.contains(AbilityMechanic.WOUNDED_TARGETS_ONLY) && target.healthFraction() >= 0.5) {
         return false;
      } else {
         return mechanics.contains(AbilityMechanic.BOSS_ONLY) && !target.boss()
            ? false
            : !mechanics.contains(AbilityMechanic.LARGE_OR_BOSS_ONLY) || target.boss() || target.large();
      }
   }

   static boolean requiresQualifiedSelection(Set<AbilityMechanic> mechanics) {
      return mechanics.contains(AbilityMechanic.RECENT_ATTACKERS)
         || mechanics.contains(AbilityMechanic.TAUNTED_TARGETS_ONLY)
         || mechanics.contains(AbilityMechanic.REQUIRES_DEFENSE_BREAK)
         || mechanics.contains(AbilityMechanic.WOUNDED_TARGETS_ONLY)
         || mechanics.contains(AbilityMechanic.LARGE_OR_BOSS_ONLY)
         || mechanics.contains(AbilityMechanic.BOSS_ONLY)
         || mechanics.contains(AbilityMechanic.MINIMUM_RANGE);
   }

   record EnemyFacts(double healthFraction, boolean boss, boolean large) {
      EnemyFacts(double healthFraction, boolean boss, boolean large) {
         if (!Double.isFinite(healthFraction)) {
            healthFraction = 1.0;
         }

         healthFraction = Math.max(0.0, Math.min(1.0, healthFraction));
         this.healthFraction = healthFraction;
         this.boss = boss;
         this.large = large;
      }
   }
}

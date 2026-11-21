package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.Objects;

final class AbilityHealingPolicy {
   private AbilityHealingPolicy() {
   }

   static double multiplier(AbilityMechanicModifiers modifiers, AbilityHealingType type, int qualifiedEnemyCount) {
      Objects.requireNonNull(modifiers, "modifiers");

      double typed = switch ((AbilityHealingType)Objects.requireNonNull(type, "type")) {
         case BURST -> modifiers.burstHealingMultiplier();
         case PERIODIC -> modifiers.periodicHealingMultiplier();
      };
      return typed * (qualifiedEnemyCount >= 2 ? modifiers.groupedEnemyHealingMultiplier() : 1.0);
   }

   static int durationTicks(int authoredTicks, AbilityHealingType type, AbilityMechanicModifiers modifiers) {
      if (authoredTicks <= 0) {
         return 0;
      } else {
         double multiplier = type == AbilityHealingType.PERIODIC ? modifiers.periodicDurationMultiplier() : 1.0;
         return (int)Math.min(2147483647L, Math.max(1L, Math.round(authoredTicks * multiplier)));
      }
   }
}

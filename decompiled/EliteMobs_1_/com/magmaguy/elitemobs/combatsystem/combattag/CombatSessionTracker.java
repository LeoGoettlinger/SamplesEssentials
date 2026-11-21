package com.magmaguy.elitemobs.combatsystem.combattag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class CombatSessionTracker {
   private final long timeoutTicks;
   private final Map<UUID, Long> combatDeadlines = new HashMap<>();

   CombatSessionTracker(long timeoutTicks) {
      if (timeoutTicks < 1L) {
         throw new IllegalArgumentException("Combat timeout must be at least one tick.");
      } else {
         this.timeoutTicks = timeoutTicks;
      }
   }

   CombatSessionTracker.DamageResult recordDamage(UUID playerId, long currentTick) {
      Long previousDeadline = this.combatDeadlines.put(playerId, currentTick + this.timeoutTicks);
      return previousDeadline != null && previousDeadline > currentTick
         ? CombatSessionTracker.DamageResult.REFRESHED_COMBAT
         : CombatSessionTracker.DamageResult.STARTED_COMBAT;
   }

   List<UUID> expire(long currentTick) {
      List<UUID> expiredPlayers = new ArrayList<>();
      this.combatDeadlines.entrySet().removeIf(entry -> {
         if (entry.getValue() > currentTick) {
            return false;
         } else {
            expiredPlayers.add(entry.getKey());
            return true;
         }
      });
      return expiredPlayers;
   }

   boolean isInCombat(UUID playerId) {
      return this.combatDeadlines.containsKey(playerId);
   }

   static enum DamageResult {
      STARTED_COMBAT,
      REFRESHED_COMBAT;
   }
}

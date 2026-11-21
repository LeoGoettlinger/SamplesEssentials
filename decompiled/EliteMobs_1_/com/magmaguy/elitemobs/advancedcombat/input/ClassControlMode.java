package com.magmaguy.elitemobs.advancedcombat.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class ClassControlMode {
   static final long TOGGLE_TAP_WINDOW_TICKS = 12L;
   private final Set<UUID> outsideEnabled = new HashSet<>();
   private final Map<UUID, Long> pendingToggleTaps = new HashMap<>();

   boolean enabled(UUID playerId, boolean alwaysAvailable, boolean outsideAllowed) {
      Objects.requireNonNull(playerId, "playerId");
      if (alwaysAvailable) {
         return true;
      } else if (!outsideAllowed) {
         this.outsideEnabled.remove(playerId);
         return false;
      } else {
         return this.outsideEnabled.contains(playerId);
      }
   }

   ClassControlMode.FAction pressF(
      UUID playerId, long currentTick, boolean alreadySneaking, boolean alwaysAvailable, boolean outsideAllowed, boolean abilityInputReady
   ) {
      Objects.requireNonNull(playerId, "playerId");
      if (!abilityInputReady) {
         this.pendingToggleTaps.remove(playerId);
         return ClassControlMode.FAction.PASS_THROUGH;
      } else if (alwaysAvailable) {
         this.pendingToggleTaps.remove(playerId);
         return ClassControlMode.FAction.OPEN_ABILITIES;
      } else if (!alreadySneaking) {
         this.pendingToggleTaps.remove(playerId);
         return this.enabled(playerId, false, outsideAllowed) ? ClassControlMode.FAction.OPEN_ABILITIES : ClassControlMode.FAction.PASS_THROUGH;
      } else {
         Long firstTap = this.pendingToggleTaps.remove(playerId);
         boolean completesDoubleTap = firstTap != null && currentTick >= firstTap && currentTick - firstTap <= 12L;
         if (!completesDoubleTap) {
            this.pendingToggleTaps.put(playerId, currentTick);
            return ClassControlMode.FAction.PASS_THROUGH;
         } else if (!outsideAllowed) {
            this.outsideEnabled.remove(playerId);
            return ClassControlMode.FAction.TOGGLE_BLOCKED;
         } else if (this.outsideEnabled.remove(playerId)) {
            return ClassControlMode.FAction.TOGGLED_OFF;
         } else {
            this.outsideEnabled.add(playerId);
            return ClassControlMode.FAction.TOGGLED_ON;
         }
      }
   }

   void clear(UUID playerId) {
      Objects.requireNonNull(playerId, "playerId");
      this.outsideEnabled.remove(playerId);
      this.pendingToggleTaps.remove(playerId);
   }

   void clearAll() {
      this.outsideEnabled.clear();
      this.pendingToggleTaps.clear();
   }

   static enum FAction {
      PASS_THROUGH,
      OPEN_ABILITIES,
      TOGGLED_ON,
      TOGGLED_OFF,
      TOGGLE_BLOCKED;
   }
}

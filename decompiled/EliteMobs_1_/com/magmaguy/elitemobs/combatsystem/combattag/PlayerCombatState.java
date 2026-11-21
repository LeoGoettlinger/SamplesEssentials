package com.magmaguy.elitemobs.combatsystem.combattag;

import java.util.UUID;

public interface PlayerCombatState {
   boolean isInCombat(UUID var1);

   void addListener(PlayerCombatState.Listener var1);

   void removeListener(PlayerCombatState.Listener var1);

   public interface Listener {
      default void onCombatStarted(UUID playerId) {
      }

      default void onCombatEnded(UUID playerId) {
      }
   }
}

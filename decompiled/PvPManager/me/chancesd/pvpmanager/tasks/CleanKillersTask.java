package me.chancesd.pvpmanager.tasks;

import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;

public class CleanKillersTask implements Runnable {
   private final PlayerManager ph;

   public CleanKillersTask(PlayerManager ph) {
      this.ph = ph;
   }

   @Override
   public final void run() {
      for (CombatPlayer p : this.ph.getPlayers().values()) {
         p.clearVictims();
      }
   }
}

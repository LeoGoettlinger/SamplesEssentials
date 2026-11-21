package me.chancesd.pvpmanager.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.chancesd.pvpmanager.PvPManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.storage.Storage;

public class StorageSaveTask implements Runnable {
   private final PvPManager plugin;
   private final Storage storage;

   public StorageSaveTask(PvPManager plugin, Storage storage) {
      this.plugin = plugin;
      this.storage = storage;
   }

   @Override
   public void run() {
      long start = System.nanoTime();
      Map<UUID, CombatPlayer> players = new HashMap<>(this.plugin.getPlayerManager().getPlayers());
      this.storage.saveUserDataBatch(players.values().stream().filter(p -> p.isLoaded()).toList());
      Log.debug("Finished saving data for " + players.size() + " players - " + (System.nanoTime() - start) / 1000000.0 + " ms");
   }
}

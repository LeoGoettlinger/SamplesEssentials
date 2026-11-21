package de.theredend2000.advancedhunt.managers;

import de.theredend2000.advancedhunt.Main;
import de.theredend2000.advancedhunt.model.TreasureCore;
import de.theredend2000.advancedhunt.util.MessageUtils;
import de.theredend2000.advancedhunt.util.PlayerSnapshot;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Location;

public class ProximityManager {
   private final Main plugin;
   private final TreasureManager treasureManager;
   private final PlayerManager playerManager;
   private boolean enabled;
   private int range;
   private int rangeSq;

   public ProximityManager(Main plugin, TreasureManager treasureManager, PlayerManager playerManager) {
      this.plugin = plugin;
      this.treasureManager = treasureManager;
      this.playerManager = playerManager;
      this.reloadConfig();
   }

   public void reloadConfig() {
      this.enabled = this.plugin.getConfig().getBoolean("proximity-settings.enabled", true);
      this.range = this.plugin.getConfig().getInt("proximity-settings.range", 10);
      this.rangeSq = this.range * this.range;
   }

   public void processTick(List<PlayerSnapshot> snapshots, Set<UUID> availableCollections) {
      if (this.enabled && this.range > 0) {
         for (PlayerSnapshot snapshot : snapshots) {
            this.checkPlayer(snapshot, availableCollections);
         }
      }
   }

   private void checkPlayer(PlayerSnapshot snapshot, Set<UUID> availableCollections) {
      Location playerLoc = snapshot.location();
      if (playerLoc != null && playerLoc.getWorld() != null) {
         int chunkX = playerLoc.getBlockX() >> 4;
         int chunkZ = playerLoc.getBlockZ() >> 4;
         int chunkRadius = (int)Math.ceil(this.range / 16.0);
         boolean foundNear = false;

         for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
            for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
               List<TreasureCore> treasures = this.treasureManager.getTreasureCoresInChunk(x, z);
               if (treasures != null && !treasures.isEmpty()) {
                  for (TreasureCore treasure : treasures) {
                     Location treasureLoc = treasure.getLocation();
                     if (treasureLoc != null
                        && treasure.isInLoadedWorld(playerLoc.getWorld())
                        && treasureLoc.distanceSquared(playerLoc) <= this.rangeSq
                        && !this.playerManager.getPlayerData(snapshot.player().getUniqueId()).hasFound(treasure.getId())
                        && availableCollections.contains(treasure.getCollectionId())) {
                        foundNear = true;
                        break;
                     }
                  }

                  if (foundNear) {
                     break;
                  }
               }
            }

            if (foundNear) {
               break;
            }
         }

         if (foundNear) {
            MessageUtils.sendActionBar(snapshot.player(), this.plugin.getMessageManager().getMessage("proximity.near_treasure"));
         }
      }
   }
}

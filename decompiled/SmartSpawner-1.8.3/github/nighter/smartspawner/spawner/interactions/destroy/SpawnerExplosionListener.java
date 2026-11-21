package github.nighter.smartspawner.spawner.interactions.destroy;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.api.events.SpawnerExplodeEvent;
import github.nighter.smartspawner.extras.HopperService;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.Iterator;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ExplosionResult;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class SpawnerExplosionListener implements Listener {
   private final SmartSpawner plugin;
   private final SpawnerManager spawnerManager;
   private boolean protectSpawners;
   private boolean protectNatural;

   public SpawnerExplosionListener(SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerManager = plugin.getSpawnerManager();
      this.loadConfig();
   }

   public void loadConfig() {
      this.protectSpawners = this.plugin.getConfig().getBoolean("spawner_properties.default.protect_from_explosions", true);
      this.protectNatural = this.plugin.getConfig().getBoolean("natural_spawner.protect_from_explosions", false);
   }

   @EventHandler
   public void onEntityExplosion(EntityExplodeEvent event) {
      this.handleExplosion(event.blockList(), event.getExplosionResult());
   }

   @EventHandler
   public void onBlockExplosion(BlockExplodeEvent event) {
      this.handleExplosion(event.blockList(), event.getExplosionResult());
   }

   private void handleExplosion(List<Block> blockList, ExplosionResult explosionResult) {
      if (explosionResult != ExplosionResult.KEEP && explosionResult != ExplosionResult.TRIGGER_BLOCK) {
         boolean hasApiListeners = SpawnerExplodeEvent.getHandlerList().getRegisteredListeners().length != 0;
         Iterator<Block> it = blockList.iterator();

         while (it.hasNext()) {
            Block block = it.next();
            Material type = block.getType();
            if (type == Material.SPAWNER) {
               if (this.plugin.getSpawnerRemovalService().isRemovalPending(block.getLocation())) {
                  it.remove();
               } else {
                  SpawnerData spawnerData = this.spawnerManager.getSpawnerByLocation(block.getLocation());
                  if (spawnerData != null) {
                     if (this.protectSpawners) {
                        it.remove();
                        this.plugin.getSpawnerGuiViewManager().closeAllViewersInventory(spawnerData);
                        this.cleanupAssociatedHopper(block);
                        if (hasApiListeners) {
                           Bukkit.getPluginManager()
                              .callEvent(new SpawnerExplodeEvent(null, spawnerData.getSpawnerLocation(), 1, false, spawnerData.getEntityType()));
                        }
                     } else {
                        spawnerData.getSpawnerStop().set(true);
                        String spawnerId = spawnerData.getSpawnerId();
                        this.cleanupAssociatedHopper(block);
                        if (hasApiListeners) {
                           Bukkit.getPluginManager()
                              .callEvent(new SpawnerExplodeEvent(null, spawnerData.getSpawnerLocation(), 1, true, spawnerData.getEntityType()));
                        }

                        this.spawnerManager.removeSpawner(spawnerId);
                        this.spawnerManager.markSpawnerDeleted(spawnerId);
                     }
                  } else if (this.protectNatural) {
                     it.remove();
                  }
               }
            } else if (type == Material.RESPAWN_ANCHOR && this.protectSpawners && this.hasProtectedSpawnersNearby(block)) {
               it.remove();
            }
         }
      }
   }

   private boolean hasProtectedSpawnersNearby(Block anchorBlock) {
      if (!this.protectSpawners) {
         return false;
      } else {
         int protectionRadius = 8;

         for (int x = -protectionRadius; x <= protectionRadius; x++) {
            for (int y = -protectionRadius; y <= protectionRadius; y++) {
               for (int z = -protectionRadius; z <= protectionRadius; z++) {
                  Block nearbyBlock = anchorBlock.getRelative(x, y, z);
                  if (nearbyBlock.getType() == Material.SPAWNER) {
                     SpawnerData spawnerData = this.spawnerManager.getSpawnerByLocation(nearbyBlock.getLocation());
                     if (spawnerData != null) {
                        return true;
                     }
                  }
               }
            }
         }

         return false;
      }
   }

   public void cleanupAssociatedHopper(Block block) {
      HopperService currentHopperService = this.plugin.getHopperService();
      if (currentHopperService != null) {
         currentHopperService.getTracker().removeBelowSpawner(block);
      }
   }
}

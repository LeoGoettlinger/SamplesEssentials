package github.nighter.smartspawner.spawner.interactions.destroy;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.commands.list.gui.management.SpawnerManagementHolder;
import github.nighter.smartspawner.extras.HopperService;
import github.nighter.smartspawner.spawner.data.SpawnerManager;
import github.nighter.smartspawner.spawner.data.storage.SpawnerStorage;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import github.nighter.smartspawner.utils.BlockPos;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class SpawnerRemovalService {
   private final SmartSpawner plugin;
   private final SpawnerManager spawnerManager;
   private final SpawnerStorage spawnerStorage;
   private final Set<String> pendingRemovalIds = ConcurrentHashMap.newKeySet();
   private final Set<BlockPos> pendingRemovalLocations = ConcurrentHashMap.newKeySet();

   public SpawnerRemovalService(SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerManager = plugin.getSpawnerManager();
      this.spawnerStorage = plugin.getSpawnerStorage();
   }

   public CompletableFuture<Boolean> removeSpawner(SpawnerData spawner) {
      if (spawner != null && spawner.getSpawnerId() != null) {
         Location location = spawner.getSpawnerLocation();
         if (location != null && location.getWorld() != null) {
            BlockPos blockPos = new BlockPos(location);
            boolean previousStop = spawner.getSpawnerStop().get();
            if (!this.claimRemoval(spawner, blockPos)) {
               return CompletableFuture.completedFuture(false);
            } else {
               CompletableFuture<Boolean> future = new CompletableFuture<>();
               World world = location.getWorld();
               int chunkX = blockPos.getChunkX();
               int chunkZ = blockPos.getChunkZ();
               Runnable removeOnLocationThread = () -> this.removeBlockAndFinalize(spawner, blockPos, previousStop, future);
               this.closeRelatedInventories(spawner);
               if (world.isChunkLoaded(chunkX, chunkZ)) {
                  Scheduler.runLocationTask(location, removeOnLocationThread);
                  return future;
               } else {
                  world.getChunkAtAsync(chunkX, chunkZ, true).whenComplete((chunk, error) -> {
                     if (error != null) {
                        this.plugin.getLogger().warning("Failed to async load chunk for spawner removal " + spawner.getSpawnerId() + ": " + error.getMessage());
                        Scheduler.runLocationTask(location, () -> this.failRemoval(spawner, blockPos, previousStop, future));
                     } else {
                        Scheduler.runLocationTask(location, removeOnLocationThread);
                     }
                  });
                  return future;
               }
            }
         } else {
            return CompletableFuture.completedFuture(false);
         }
      } else {
         return CompletableFuture.completedFuture(false);
      }
   }

   public boolean isRemovalPending(SpawnerData spawner) {
      return spawner != null && this.pendingRemovalIds.contains(spawner.getSpawnerId());
   }

   public boolean isRemovalPending(Location location) {
      return location != null && location.getWorld() != null && this.pendingRemovalLocations.contains(new BlockPos(location));
   }

   private boolean claimRemoval(SpawnerData spawner, BlockPos blockPos) {
      Location location = spawner.getSpawnerLocation();
      if (spawner.isSelling()) {
         return false;
      } else {
         SpawnerData currentById = this.spawnerManager.getSpawnerById(spawner.getSpawnerId());
         SpawnerData currentByLocation = this.spawnerManager.getSpawnerByLocation(location);
         if (currentById == spawner && currentByLocation == spawner) {
            if (!this.pendingRemovalIds.add(spawner.getSpawnerId())) {
               return false;
            } else if (!this.pendingRemovalLocations.add(blockPos)) {
               this.pendingRemovalIds.remove(spawner.getSpawnerId());
               return false;
            } else {
               spawner.getSpawnerStop().set(true);
               return true;
            }
         } else {
            return false;
         }
      }
   }

   private void removeBlockAndFinalize(SpawnerData spawner, BlockPos blockPos, boolean previousStop, CompletableFuture<Boolean> future) {
      if (!this.pendingRemovalIds.contains(spawner.getSpawnerId())) {
         this.completeFuture(future, false);
      } else {
         Location location = spawner.getSpawnerLocation();
         World world = location.getWorld();
         if (world != null && world.isChunkLoaded(blockPos.getChunkX(), blockPos.getChunkZ())) {
            try {
               SpawnerData currentSpawner = this.spawnerManager.getSpawnerById(spawner.getSpawnerId());
               if (currentSpawner == spawner) {
                  Block block = location.getBlock();
                  if (this.plugin.getRangeChecker() != null) {
                     this.plugin.getRangeChecker().deactivateSpawner(spawner);
                  }

                  this.cleanupAssociatedHopper(block);
                  if (block.getType() == Material.SPAWNER) {
                     block.setType(Material.AIR);
                  }

                  spawner.getSpawnerStop().set(true);
                  this.spawnerManager.removeSpawner(spawner.getSpawnerId());
                  this.spawnerStorage.markSpawnerDeleted(spawner.getSpawnerId());
                  this.completeFuture(future, true);
                  return;
               }

               this.completeFuture(future, false);
            } finally {
               this.pendingRemovalIds.remove(spawner.getSpawnerId());
               this.pendingRemovalLocations.remove(blockPos);
            }
         } else {
            this.failRemoval(spawner, blockPos, previousStop, future);
         }
      }
   }

   private void failRemoval(SpawnerData spawner, BlockPos blockPos, boolean previousStop, CompletableFuture<Boolean> future) {
      this.pendingRemovalIds.remove(spawner.getSpawnerId());
      this.pendingRemovalLocations.remove(blockPos);
      spawner.getSpawnerStop().set(previousStop);
      this.completeFuture(future, false);
   }

   private void closeRelatedInventories(SpawnerData spawner) {
      this.plugin.getSpawnerGuiViewManager().closeAllViewersInventory(spawner);
      String spawnerId = spawner.getSpawnerId();

      for (Player player : Bukkit.getOnlinePlayers()) {
         Scheduler.runEntityTask(player, () -> this.closeManagementInventory(player, spawnerId));
      }
   }

   private void closeManagementInventory(Player player, String spawnerId) {
      if (player.isOnline()) {
         Inventory inventory = player.getOpenInventory().getTopInventory();
         if (inventory != null) {
            if (inventory.getHolder(false) instanceof SpawnerManagementHolder managementHolder && spawnerId.equals(managementHolder.getSpawnerId())) {
               player.closeInventory();
            }
         }
      }
   }

   private void cleanupAssociatedHopper(Block block) {
      HopperService hopperService = this.plugin.getHopperService();
      if (hopperService != null) {
         hopperService.getTracker().removeBelowSpawner(block);
      }
   }

   private void completeFuture(CompletableFuture<Boolean> future, boolean value) {
      if (!future.isDone()) {
         future.complete(value);
      }
   }
}

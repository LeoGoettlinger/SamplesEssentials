package github.nighter.smartspawner.spawner.data;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.config.SpawnerDisplayConfigurator;
import github.nighter.smartspawner.spawner.data.storage.SpawnerStorage;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;

public class SpawnerManager {
   private final SmartSpawner plugin;
   private final Map<String, SpawnerData> spawners = new ConcurrentHashMap<>();
   private final Map<SpawnerManager.LocationKey, SpawnerData> locationIndex = new HashMap<>();
   private final Map<String, Set<SpawnerData>> worldIndex = new HashMap<>();
   private final SpawnerStorage spawnerStorage;
   private final Set<String> confirmedGhostSpawners = ConcurrentHashMap.newKeySet();

   public SpawnerManager(SmartSpawner plugin) {
      this.plugin = plugin;
      this.spawnerStorage = plugin.getSpawnerStorage();
      this.initializeWithoutLoading();
   }

   public void reloadSpawnerDrops() {
      for (SpawnerData spawner : this.getAllSpawners()) {
         try {
            spawner.setLootConfig();
         } catch (Exception var5) {
            this.plugin.getLogger().warning("Failed to reload drops config for spawner " + spawner.getSpawnerId() + ": " + var5.getMessage());
         }
      }
   }

   public void reloadSpawnerDropsAndConfigs() {
      for (SpawnerData spawner : this.getAllSpawners()) {
         try {
            spawner.loadConfigurationValues();
            spawner.recalculateAfterConfigReload();
            this.refreshSpawnerDisplay(spawner);
         } catch (Exception var5) {
            this.plugin.getLogger().warning("Failed to reload config for spawner " + spawner.getSpawnerId() + ": " + var5.getMessage());
         }
      }
   }

   private void refreshSpawnerDisplay(SpawnerData spawner) {
      Location location = spawner.getSpawnerLocation();
      Scheduler.runLocationTask(location, () -> {
         Block block = location.getBlock();
         if (block.getType() == Material.SPAWNER && block.getState(false) instanceof CreatureSpawner state) {
            if (spawner.isItemSpawner()) {
               SpawnerDisplayConfigurator.applyItem(this.plugin, state, spawner.getConfigName(), spawner.getSpawnedItemMaterial());
            } else {
               SpawnerDisplayConfigurator.applyMob(this.plugin, state, spawner.getConfigName(), spawner.getEntityType());
            }

            state.update(true, false);
         }
      });
   }

   public void addSpawner(String id, SpawnerData spawner) {
      this.spawners.put(id, spawner);
      this.locationIndex.put(new SpawnerManager.LocationKey(spawner.getSpawnerLocation()), spawner);
      String worldName = spawner.getSpawnerLocation().getWorld().getName();
      this.worldIndex.computeIfAbsent(worldName, k -> new HashSet<>()).add(spawner);
      this.spawnerStorage.queueSpawnerForSaving(id);
   }

   public void removeSpawner(String id) {
      SpawnerData spawner = this.spawners.get(id);
      if (spawner != null) {
         Location loc = spawner.getSpawnerLocation();
         Scheduler.runLocationTask(loc, spawner::removeHologram);
         this.locationIndex.remove(new SpawnerManager.LocationKey(spawner.getSpawnerLocation()));
         String worldName = spawner.getSpawnerLocation().getWorld().getName();
         Set<SpawnerData> worldSpawners = this.worldIndex.get(worldName);
         if (worldSpawners != null) {
            worldSpawners.remove(spawner);
            if (worldSpawners.isEmpty()) {
               this.worldIndex.remove(worldName);
            }
         }

         this.spawners.remove(id);
      }
   }

   public int countSpawnersInWorld(String worldName) {
      Set<SpawnerData> worldSpawners = this.worldIndex.get(worldName);
      return worldSpawners != null ? worldSpawners.size() : 0;
   }

   public int countTotalSpawnersWithStacks(String worldName) {
      Set<SpawnerData> worldSpawners = this.worldIndex.get(worldName);
      return worldSpawners == null ? 0 : worldSpawners.stream().mapToInt(SpawnerData::getStackSize).sum();
   }

   public SpawnerData getSpawnerByLocation(Location location) {
      return this.locationIndex.get(new SpawnerManager.LocationKey(location));
   }

   public SpawnerData getSpawnerById(String id) {
      return this.spawners.get(id);
   }

   public List<SpawnerData> getAllSpawners() {
      return new ArrayList<>(this.spawners.values());
   }

   public void addSpawnerToIndexes(String spawnerId, SpawnerData spawner) {
      this.spawners.put(spawnerId, spawner);
      this.locationIndex.put(new SpawnerManager.LocationKey(spawner.getSpawnerLocation()), spawner);
      String worldName = spawner.getSpawnerLocation().getWorld().getName();
      this.worldIndex.computeIfAbsent(worldName, k -> new HashSet<>()).add(spawner);
   }

   public Set<SpawnerData> getSpawnersInWorld(String worldName) {
      return this.worldIndex.get(worldName);
   }

   public Set<String> unloadSpawnersInWorld(String worldName) {
      Set<SpawnerData> worldSpawners = this.worldIndex.get(worldName);
      if (worldSpawners != null && !worldSpawners.isEmpty()) {
         Set<String> removedSpawnerIds = new HashSet<>();

         for (SpawnerData spawner : new HashSet<>(worldSpawners)) {
            spawner.removeHologram();
            removedSpawnerIds.add(spawner.getSpawnerId());
            this.spawners.remove(spawner.getSpawnerId());
            this.locationIndex.entrySet().removeIf(entry -> entry.getValue() == spawner);
         }

         this.worldIndex.remove(worldName);
         return removedSpawnerIds;
      } else {
         return Collections.emptySet();
      }
   }

   public void initializeWithoutLoading() {
      this.spawners.clear();
      this.locationIndex.clear();
      this.worldIndex.clear();
      this.confirmedGhostSpawners.clear();
   }

   public boolean isGhostSpawner(SpawnerData spawner) {
      if (spawner == null) {
         return false;
      } else if (this.confirmedGhostSpawners.contains(spawner.getSpawnerId())) {
         return true;
      } else {
         Location loc = spawner.getSpawnerLocation();
         if (loc != null && loc.getWorld() != null) {
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;
            return !loc.getWorld().isChunkLoaded(chunkX, chunkZ) ? false : loc.getBlock().getType() != Material.SPAWNER;
         } else {
            return true;
         }
      }
   }

   public void removeGhostSpawner(String spawnerId) {
      SpawnerData spawner = this.spawners.get(spawnerId);
      if (spawner != null) {
         Location loc = spawner.getSpawnerLocation();
         this.confirmedGhostSpawners.add(spawnerId);
         Scheduler.runLocationTask(loc, () -> {
            spawner.removeHologram();
            Scheduler.runTask(() -> {
               this.removeSpawner(spawnerId);
               this.spawnerStorage.markSpawnerDeleted(spawnerId);
            });
         });
      }
   }

   public void markSpawnerModified(String spawnerId) {
      this.spawnerStorage.markSpawnerModified(spawnerId);
   }

   public void markSpawnerDeleted(String spawnerId) {
      this.spawnerStorage.markSpawnerDeleted(spawnerId);
   }

   public void queueSpawnerForSaving(String spawnerId) {
      this.spawnerStorage.queueSpawnerForSaving(spawnerId);
   }

   public void refreshAllHolograms() {
      for (SpawnerData spawner : this.spawners.values()) {
         Location loc = spawner.getSpawnerLocation();
         Scheduler.runLocationTask(loc, spawner::refreshHologram);
      }
   }

   public void reloadAllHolograms() {
      if (this.plugin.getConfig().getBoolean("hologram.enabled", false)) {
         for (SpawnerData spawner : this.spawners.values()) {
            Location loc = spawner.getSpawnerLocation();
            Scheduler.runLocationTask(loc, spawner::reloadHologramData);
         }
      }
   }

   public void cleanupAllSpawners() {
      this.spawners.clear();
      this.locationIndex.clear();
      this.worldIndex.clear();
      this.confirmedGhostSpawners.clear();
   }

   public int getTotalSpawners() {
      return this.spawners.size();
   }

   private static class LocationKey {
      private final String world;
      private final int x;
      private final int y;
      private final int z;

      public LocationKey(Location location) {
         this.world = location.getWorld().getName();
         this.x = location.getBlockX();
         this.y = location.getBlockY();
         this.z = location.getBlockZ();
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof SpawnerManager.LocationKey that)
               ? false
               : this.x == that.x && this.y == that.y && this.z == that.z && this.world.equals(that.world);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.world, this.x, this.y, this.z);
      }
   }
}

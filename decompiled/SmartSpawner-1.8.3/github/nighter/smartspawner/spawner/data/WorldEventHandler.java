package github.nighter.smartspawner.spawner.data;

import github.nighter.smartspawner.Scheduler;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.properties.SpawnerData;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

public class WorldEventHandler implements Listener {
   private final SmartSpawner plugin;
   private final Logger logger;
   private final Set<String> processedWorlds = ConcurrentHashMap.newKeySet();
   private final Map<String, WorldEventHandler.PendingSpawnerData> pendingSpawners = new ConcurrentHashMap<>();
   private volatile boolean initialLoadAttempted = false;

   public WorldEventHandler(SmartSpawner plugin) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onWorldLoad(WorldLoadEvent event) {
      World world = event.getWorld();
      String worldName = world.getName();
      this.processedWorlds.add(worldName);
      this.loadPendingSpawnersForWorld(worldName);
      if (!this.initialLoadAttempted) {
         Scheduler.runTaskLater(() -> this.attemptInitialSpawnerLoad(), 20L);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onWorldSave(WorldSaveEvent event) {
      World world = event.getWorld();
      this.plugin.getSpawnerStorage().flushChanges();
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onWorldUnload(WorldUnloadEvent event) {
      World world = event.getWorld();
      String worldName = world.getName();
      this.processedWorlds.remove(worldName);
      this.plugin.getSpawnerStorage().flushChanges();
      this.unloadSpawnersFromWorld(worldName);
   }

   public void attemptInitialSpawnerLoad() {
      if (!this.initialLoadAttempted) {
         this.initialLoadAttempted = true;
         Map<String, SpawnerData> allSpawnerData = this.plugin.getSpawnerStorage().loadAllSpawnersRaw();
         int loadedCount = 0;
         int pendingCount = 0;

         for (Entry<String, SpawnerData> entry : allSpawnerData.entrySet()) {
            String spawnerId = entry.getKey();
            SpawnerData spawner = entry.getValue();
            if (spawner != null) {
               String worldName = spawner.getSpawnerLocation() != null && spawner.getSpawnerLocation().getWorld() != null
                  ? spawner.getSpawnerLocation().getWorld().getName()
                  : null;
               if (worldName != null && Bukkit.getWorld(worldName) != null) {
                  this.plugin.getSpawnerManager().addSpawnerToIndexes(spawnerId, spawner);
                  loadedCount++;
               } else {
                  WorldEventHandler.PendingSpawnerData pending = this.loadPendingSpawnerFromFile(spawnerId);
                  if (pending != null) {
                     this.pendingSpawners.put(spawnerId, pending);
                     pendingCount++;
                  }
               }
            } else {
               WorldEventHandler.PendingSpawnerData pending = this.loadPendingSpawnerFromFile(spawnerId);
               if (pending != null) {
                  this.pendingSpawners.put(spawnerId, pending);
                  pendingCount++;
               }
            }
         }

         this.logger.info("Initial spawner load complete. Loaded: " + loadedCount + ", Pending (missing worlds): " + pendingCount);
         if (pendingCount > 0) {
            this.logger.info("Pending spawners will be loaded when their worlds become available.");
         }
      }
   }

   private void loadPendingSpawnersForWorld(String worldName) {
      if (!this.pendingSpawners.isEmpty()) {
         int loadedCount = 0;

         for (String spawnerId : new HashSet<>(this.pendingSpawners.keySet())) {
            WorldEventHandler.PendingSpawnerData pending = this.pendingSpawners.get(spawnerId);
            if (pending != null && worldName.equals(pending.worldName)) {
               SpawnerData spawner = this.plugin.getSpawnerStorage().loadSpecificSpawner(spawnerId);
               if (spawner != null) {
                  this.plugin.getSpawnerManager().addSpawnerToIndexes(spawnerId, spawner);
                  this.pendingSpawners.remove(spawnerId);
                  loadedCount++;
               }
            }
         }

         if (loadedCount > 0) {
            this.logger.info("Loaded " + loadedCount + " pending spawners for world: " + worldName);
         }
      }
   }

   private void unloadSpawnersFromWorld(String worldName) {
      Set<String> unloadedSpawnerIds = this.plugin.getSpawnerManager().unloadSpawnersInWorld(worldName);
      if (!unloadedSpawnerIds.isEmpty()) {
         for (String spawnerId : unloadedSpawnerIds) {
            this.pendingSpawners.put(spawnerId, new WorldEventHandler.PendingSpawnerData(worldName));
         }

         this.logger.info("Unloaded " + unloadedSpawnerIds.size() + " spawners from world: " + worldName);
      }
   }

   private WorldEventHandler.PendingSpawnerData loadPendingSpawnerFromFile(String spawnerId) {
      try {
         String locationString = this.plugin.getSpawnerStorage().getRawLocationString(spawnerId);
         if (locationString != null) {
            String[] locParts = locationString.split(",");
            if (locParts.length >= 1) {
               String worldName = locParts[0].trim();
               if (!worldName.isEmpty()) {
                  return new WorldEventHandler.PendingSpawnerData(worldName);
               }
            }
         }
      } catch (Exception var5) {
      }

      return null;
   }

   public boolean isWorldLoaded(String worldName) {
      return this.processedWorlds.contains(worldName) && Bukkit.getWorld(worldName) != null;
   }

   public int getPendingSpawnerCount() {
      return this.pendingSpawners.size();
   }

   private static class PendingSpawnerData {
      final String worldName;

      PendingSpawnerData(String worldName) {
         this.worldName = worldName;
      }
   }
}

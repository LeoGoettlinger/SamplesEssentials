package cm.chunkManager.components;

import cm.chunkManager.ChunkManager;
import cm.chunkManager.hooks.WgBridge;
import cm.chunkManager.utils.Scheduler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class SmartChunkUnloader {
   private final ChunkManager plugin;
   private final Map<SmartChunkUnloader.ChunkIdentifier, SmartChunkUnloader.ChunkHeatData> heatMap;
   private final Map<UUID, SmartChunkUnloader.PlayerMovementData> playerMovement;
   private final ChunkProcessor chunkProcessor;
   private final File dataFile;
   private double heatDecayRate;
   private double visitHeatIncrease;
   private double proximityHeatBonus;
   private double coldThreshold;
   private long minChunkAge;
   private int defaultSafetyRadius;
   private long heatDecayInterval;

   public SmartChunkUnloader(ChunkManager plugin, ChunkProcessor processor) {
      this.plugin = plugin;
      this.chunkProcessor = processor;
      this.heatMap = new ConcurrentHashMap<>();
      this.playerMovement = new ConcurrentHashMap<>();
      this.dataFile = new File(plugin.getDataFolder(), "heatmap.yml");
      this.loadConfig();
      this.loadHeatMapData();
      this.startHeatDecayTask();
      this.startMovementTracker();
   }

   private void loadConfig() {
      this.heatDecayRate = this.plugin.getConfig().getDouble("smart-unloading.heat-decay-rate", 0.95);
      this.visitHeatIncrease = this.plugin.getConfig().getDouble("smart-unloading.visit-heat-increase", 10.0);
      this.proximityHeatBonus = this.plugin.getConfig().getDouble("smart-unloading.proximity-heat-bonus", 5.0);
      this.coldThreshold = this.plugin.getConfig().getDouble("smart-unloading.cold-threshold", 1.0);
      this.minChunkAge = this.plugin.getConfig().getLong("smart-unloading.min-chunk-age", 30000L);
      this.defaultSafetyRadius = this.plugin.getConfig().getInt("smart-unloading.safety-radius", 3);
      this.heatDecayInterval = this.plugin.getConfig().getLong("smart-unloading.heat-decay-interval", 5000L) / 50L;
   }

   public void reloadConfig() {
      this.loadConfig();
   }

   private void startHeatDecayTask() {
      long interval = Math.max(20L, this.heatDecayInterval);
      Scheduler.runAsyncTimer(this.plugin, this::decayHeatValues, interval, interval);
   }

   private void startMovementTracker() {
      Scheduler.runGlobalTimer(this.plugin, () -> {
         for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            this.trackPlayerMovement(player);
         }
      }, 20L, 20L);
   }

   private void trackPlayerMovement(Player player) {
      Location loc = player.getLocation();
      if (loc.getWorld() != null) {
         SmartChunkUnloader.PlayerMovementData data = this.playerMovement
            .computeIfAbsent(player.getUniqueId(), k -> new SmartChunkUnloader.PlayerMovementData());
         data.addLocation(loc);
         this.updateNearbyChunkHeat(loc, this.proximityHeatBonus);

         for (SmartChunkUnloader.ChunkIdentifier id : data.getPredictedChunkIds(2)) {
            this.increaseHeatById(id, this.proximityHeatBonus * 0.5);
         }
      }
   }

   public void onChunkLoad(Chunk chunk) {
      SmartChunkUnloader.ChunkIdentifier id = new SmartChunkUnloader.ChunkIdentifier(chunk);
      SmartChunkUnloader.ChunkHeatData data = this.heatMap.computeIfAbsent(id, k -> new SmartChunkUnloader.ChunkHeatData());
      data.recordVisit();
      this.updateChunkEntityData(chunk, data);
   }

   public void onChunkAccess(Chunk chunk) {
      SmartChunkUnloader.ChunkIdentifier id = new SmartChunkUnloader.ChunkIdentifier(chunk);
      SmartChunkUnloader.ChunkHeatData data = this.heatMap.computeIfAbsent(id, k -> new SmartChunkUnloader.ChunkHeatData());
      data.recordVisit();
      this.increaseHeatById(id, this.visitHeatIncrease);
   }

   private void updateChunkEntityData(Chunk chunk, SmartChunkUnloader.ChunkHeatData data) {
      if (!Scheduler.isFolia()) {
         try {
            int entities = chunk.getEntities().length;
            int tileEntities = chunk.getTileEntities().length;
            data.entityCount = entities + tileEntities;
            data.hasImportantEntities = this.hasImportantEntities(chunk);
            if (data.hasImportantEntities) {
               data.updateHeat(data.getHeatValue() * 2.0);
            }
         } catch (Exception var5) {
         }
      }
   }

   private boolean hasImportantEntities(Chunk chunk) {
      if (Scheduler.isFolia()) {
         return false;
      } else {
         try {
            return Arrays.stream(chunk.getEntities())
                  .anyMatch(entity -> entity instanceof Villager || entity instanceof IronGolem || entity.getCustomName() != null)
               || chunk.getTileEntities().length > 5;
         } catch (Exception var3) {
            return false;
         }
      }
   }

   private void increaseHeatById(SmartChunkUnloader.ChunkIdentifier id, double amount) {
      SmartChunkUnloader.ChunkHeatData data = this.heatMap.computeIfAbsent(id, k -> new SmartChunkUnloader.ChunkHeatData());
      double entityMultiplier = Math.max(1.0, Math.min(10.0, 1.0 + data.entityCount * 0.1));
      double adjustedAmount = Math.max(0.0, amount * entityMultiplier);
      data.updateHeat(data.getHeatValue() + adjustedAmount);
   }

   private void updateNearbyChunkHeat(Location location, double baseHeat) {
      if (location != null) {
         World world = location.getWorld();
         if (world != null) {
            int centerX = location.getBlockX() >> 4;
            int centerZ = location.getBlockZ() >> 4;
            String worldName = world.getName();

            for (int dx = -2; dx <= 2; dx++) {
               for (int dz = -2; dz <= 2; dz++) {
                  double distance = Math.sqrt(dx * dx + dz * dz);
                  double heat = baseHeat / (1.0 + distance);
                  if (world.isChunkLoaded(centerX + dx, centerZ + dz)) {
                     this.increaseHeatById(new SmartChunkUnloader.ChunkIdentifier(worldName, centerX + dx, centerZ + dz), heat);
                  }
               }
            }
         }
      }
   }

   private void decayHeatValues() {
      this.heatMap.forEach((id, data) -> {
         double currentHeat = data.getHeatValue();
         if (!(currentHeat <= 0.0)) {
            long timeSinceAccess = data.getTimeSinceLastAccess();
            if (timeSinceAccess >= 0L) {
               double timeFactor = Math.min(1.0, Math.max(0.0, timeSinceAccess / 60000.0));
               double decayMultiplier = Math.pow(this.heatDecayRate, timeFactor);
               double newHeat = currentHeat * decayMultiplier;
               if (data.hasImportantEntities) {
                  newHeat = Math.max(newHeat, 5.0);
               }

               data.updateHeat(newHeat);
            }
         }
      });
   }

   public List<SmartChunkUnloader.ChunkIdentifier> identifyColdChunks(World world, int maxChunks) {
      ChunkFreezer freezer = this.plugin.getChunkFreezer();
      List<Entry<SmartChunkUnloader.ChunkIdentifier, Double>> coldChunkSnapshot = this.heatMap
         .entrySet()
         .stream()
         .filter(entry -> entry.getKey().worldName.equals(world.getName()))
         .filter(entry -> freezer == null || !freezer.isFrozen(entry.getKey().worldName, entry.getKey().x, entry.getKey().z))
         .filter(entry -> {
            SmartChunkUnloader.ChunkHeatData data = entry.getValue();
            return data.getHeatValue() < this.coldThreshold && data.getAge() > this.minChunkAge && !data.hasImportantEntities;
         })
         .map(entry -> new SimpleEntry<>(entry.getKey(), entry.getValue().getHeatValue()))
         .collect(Collectors.toList());
      coldChunkSnapshot.sort(Comparator.comparingDouble(Entry::getValue));
      return coldChunkSnapshot.stream().limit(maxChunks).map(Entry::getKey).collect(Collectors.toList());
   }

   public int performSmartUnload(World world, int targetUnloadCount) {
      if (world != null && targetUnloadCount > 0) {
         WorldSettingsManager wsm = this.plugin.getWorldSettingsManager();
         if (wsm != null) {
            WorldSettingsManager.WorldSettings settings = wsm.getSettings(world);
            if (!settings.enabled || !settings.smartUnloading) {
               return 0;
            }

            targetUnloadCount = Math.min(targetUnloadCount, settings.maxChunksPerTick);
         }

         List<SmartChunkUnloader.ChunkIdentifier> coldChunks = this.identifyColdChunks(world, targetUnloadCount);
         AtomicInteger unloadedCount = new AtomicInteger(0);
         if (Scheduler.isFolia()) {
            int scheduled = 0;

            for (SmartChunkUnloader.ChunkIdentifier id : coldChunks) {
               if (this.isChunkSafeToUnload(world, id)) {
                  Location loc = new Location(world, (id.x << 4) + 8, 64.0, (id.z << 4) + 8);
                  Scheduler.runAt(this.plugin, loc, () -> {
                     if (world.isChunkLoaded(id.x, id.z)) {
                        Chunk chunkx = world.getChunkAt(id.x, id.z);
                        if (chunkx.isLoaded()) {
                           chunkx.unload(true);
                           this.heatMap.remove(id);
                           unloadedCount.incrementAndGet();
                        }
                     }
                  });
                  scheduled++;
               }
            }

            return scheduled;
         } else {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            for (SmartChunkUnloader.ChunkIdentifier idx : coldChunks) {
               if (world.isChunkLoaded(idx.x, idx.z)) {
                  Chunk chunk = world.getChunkAt(idx.x, idx.z);
                  if (chunk.isLoaded() && this.isChunkSafeToUnload(chunk)) {
                     CompletableFuture<Void> future = this.chunkProcessor.submitUnloadTask(chunk).thenRun(() -> {
                        this.heatMap.remove(id);
                        unloadedCount.incrementAndGet();
                     });
                     futures.add(future);
                  }
               }
            }

            if (!futures.isEmpty()) {
               try {
                  CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
               } catch (Exception var11) {
               }
            }

            return unloadedCount.get();
         }
      } else {
         return 0;
      }
   }

   private boolean isChunkSafeToUnload(Chunk chunk) {
      ChunkFreezer freezer = this.plugin.getChunkFreezer();
      if (freezer != null && freezer.isFrozen(chunk.getWorld().getName(), chunk.getX(), chunk.getZ())) {
         return false;
      } else if (this.plugin.getConfigManager().isWgCheckOnUnload() && WgBridge.isChunkProtected(chunk, this.plugin.getConfigManager().getWgRegionWhitelist())) {
         return false;
      } else {
         World world = chunk.getWorld();
         WorldSettingsManager wsm = this.plugin.getWorldSettingsManager();
         int radius = wsm != null ? wsm.getSettings(world).safetyRadius : this.defaultSafetyRadius;

         for (Player player : world.getPlayers()) {
            Location playerLoc = player.getLocation();
            if (playerLoc.getWorld() != null) {
               int playerChunkX = playerLoc.getBlockX() >> 4;
               int playerChunkZ = playerLoc.getBlockZ() >> 4;
               int distance = Math.max(Math.abs(chunk.getX() - playerChunkX), Math.abs(chunk.getZ() - playerChunkZ));
               if (distance <= radius) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   private boolean isChunkSafeToUnload(World world, SmartChunkUnloader.ChunkIdentifier id) {
      ChunkFreezer freezer = this.plugin.getChunkFreezer();
      if (freezer != null && freezer.isFrozen(id.worldName, id.x, id.z)) {
         return false;
      } else {
         WorldSettingsManager wsm = this.plugin.getWorldSettingsManager();
         int radius = wsm != null ? wsm.getSettings(world).safetyRadius : this.defaultSafetyRadius;

         for (Player player : world.getPlayers()) {
            Location playerLoc = player.getLocation();
            if (playerLoc.getWorld() != null) {
               int playerChunkX = playerLoc.getBlockX() >> 4;
               int playerChunkZ = playerLoc.getBlockZ() >> 4;
               int distance = Math.max(Math.abs(id.x - playerChunkX), Math.abs(id.z - playerChunkZ));
               if (distance <= radius) {
                  return false;
               }
            }
         }

         return true;
      }
   }

   public double getChunkHeat(String world, int x, int z) {
      SmartChunkUnloader.ChunkIdentifier id = new SmartChunkUnloader.ChunkIdentifier(world, x, z);
      SmartChunkUnloader.ChunkHeatData data = this.heatMap.get(id);
      return data != null ? data.getHeatValue() : 0.0;
   }

   public Map<String, Object> getHeatMapStats() {
      Map<String, Object> stats = new HashMap<>();
      double totalHeat = this.heatMap.values().stream().mapToDouble(SmartChunkUnloader.ChunkHeatData::getHeatValue).filter(Double::isFinite).sum();
      double averageHeat = this.heatMap.isEmpty() ? 0.0 : totalHeat / this.heatMap.size();
      long coldChunks = this.heatMap.values().stream().filter(data -> data.getHeatValue() < this.coldThreshold).count();
      long hotChunks = this.heatMap.values().stream().filter(data -> data.getHeatValue() > 50.0).count();
      stats.put("totalChunks", this.heatMap.size());
      stats.put("averageHeat", averageHeat);
      stats.put("coldChunks", coldChunks);
      stats.put("hotChunks", hotChunks);
      stats.put("totalHeat", totalHeat);
      return stats;
   }

   public void onPlayerQuit(Player player) {
      this.playerMovement.remove(player.getUniqueId());
   }

   public void saveHeatMapData() {
      if (!this.heatMap.isEmpty()) {
         YamlConfiguration data = new YamlConfiguration();
         int count = 0;

         for (Entry<SmartChunkUnloader.ChunkIdentifier, SmartChunkUnloader.ChunkHeatData> entry : this.heatMap.entrySet()) {
            SmartChunkUnloader.ChunkIdentifier id = entry.getKey();
            SmartChunkUnloader.ChunkHeatData heat = entry.getValue();
            String key = "chunks." + id.toKey().replace(";", "_");
            data.set(key + ".heat", heat.getHeatValue());
            data.set(key + ".lastAccess", heat.getLastAccess());
            data.set(key + ".loadTime", heat.getLoadTime());
            data.set(key + ".visits", heat.getVisitCount());
            count++;
         }

         data.set("meta.count", count);
         data.set("meta.saved", System.currentTimeMillis());

         try {
            data.save(this.dataFile);
            this.plugin.getLogger().info("Saved " + count + " heat map entries");
         } catch (IOException var8) {
            this.plugin.getLogger().warning("Failed to save heat map data: " + var8.getMessage());
         }
      }
   }

   public void loadHeatMapData() {
      if (this.dataFile.exists()) {
         YamlConfiguration data = YamlConfiguration.loadConfiguration(this.dataFile);
         int loaded = 0;
         if (data.contains("chunks")) {
            for (String key : data.getConfigurationSection("chunks").getKeys(false)) {
               String path = "chunks." + key;
               String realKey = key.replace("_", ";");
               SmartChunkUnloader.ChunkIdentifier id = SmartChunkUnloader.ChunkIdentifier.fromKey(realKey);
               if (id != null) {
                  double heat = data.getDouble(path + ".heat", 5.0);
                  long lastAccess = data.getLong(path + ".lastAccess", System.currentTimeMillis());
                  long loadTime = data.getLong(path + ".loadTime", System.currentTimeMillis());
                  int visits = data.getInt(path + ".visits", 0);
                  this.heatMap.put(id, new SmartChunkUnloader.ChunkHeatData(heat, lastAccess, loadTime, visits));
                  loaded++;
               }
            }
         }

         if (loaded > 0) {
            this.plugin.getLogger().info("Loaded " + loaded + " heat map entries");
         }
      }
   }

   public void cleanup() {
      this.saveHeatMapData();
      this.heatMap.clear();
      this.playerMovement.clear();
   }

   public static class ChunkHeatData {
      private final AtomicLong lastAccess;
      private final AtomicLong loadTime;
      private final AtomicInteger visitCount;
      private volatile double heatValue;
      private volatile double peakHeat;
      private volatile boolean hasImportantEntities;
      private volatile int entityCount;

      public ChunkHeatData() {
         this.lastAccess = new AtomicLong(System.currentTimeMillis());
         this.loadTime = new AtomicLong(System.currentTimeMillis());
         this.visitCount = new AtomicInteger(0);
         this.heatValue = 5.0;
         this.peakHeat = 5.0;
      }

      public ChunkHeatData(double heat, long lastAccess, long loadTime, int visits) {
         this.lastAccess = new AtomicLong(lastAccess);
         this.loadTime = new AtomicLong(loadTime);
         this.visitCount = new AtomicInteger(visits);
         this.heatValue = heat;
         this.peakHeat = heat;
      }

      public void recordVisit() {
         this.lastAccess.set(System.currentTimeMillis());
         this.visitCount.incrementAndGet();
      }

      public void updateHeat(double newHeat) {
         if (!Double.isFinite(newHeat)) {
            this.heatValue = 0.0;
         } else {
            this.heatValue = Math.max(0.0, Math.min(1000.0, newHeat));
            if (this.heatValue > this.peakHeat) {
               this.peakHeat = this.heatValue;
            }
         }
      }

      public double getHeatValue() {
         double heat = this.heatValue;
         return Double.isFinite(heat) ? heat : 0.0;
      }

      public long getAge() {
         return System.currentTimeMillis() - this.loadTime.get();
      }

      public long getTimeSinceLastAccess() {
         return System.currentTimeMillis() - this.lastAccess.get();
      }

      public int getVisitCount() {
         return this.visitCount.get();
      }

      public long getLastAccess() {
         return this.lastAccess.get();
      }

      public long getLoadTime() {
         return this.loadTime.get();
      }
   }

   public static class ChunkIdentifier {
      public final String worldName;
      public final int x;
      public final int z;

      public ChunkIdentifier(Chunk chunk) {
         this.worldName = chunk.getWorld().getName();
         this.x = chunk.getX();
         this.z = chunk.getZ();
      }

      public ChunkIdentifier(String worldName, int x, int z) {
         this.worldName = worldName;
         this.x = x;
         this.z = z;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof SmartChunkUnloader.ChunkIdentifier that)
               ? false
               : this.x == that.x && this.z == that.z && this.worldName.equals(that.worldName);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.worldName, this.x, this.z);
      }

      public String toKey() {
         return this.worldName + ";" + this.x + ";" + this.z;
      }

      public static SmartChunkUnloader.ChunkIdentifier fromKey(String key) {
         if (key.contains(";")) {
            String[] parts = key.split(";");
            return parts.length != 3 ? null : new SmartChunkUnloader.ChunkIdentifier(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
         } else {
            int lastColon = key.lastIndexOf(58);
            if (lastColon < 0) {
               return null;
            } else {
               int secondColon = key.lastIndexOf(58, lastColon - 1);
               if (secondColon < 0) {
                  return null;
               } else {
                  String world = key.substring(0, secondColon);
                  int xVal = Integer.parseInt(key.substring(secondColon + 1, lastColon));
                  int zVal = Integer.parseInt(key.substring(lastColon + 1));
                  return new SmartChunkUnloader.ChunkIdentifier(world, xVal, zVal);
               }
            }
         }
      }
   }

   public static class PlayerMovementData {
      private final Queue<Location> recentLocations;
      private final int maxHistory = 50;
      private Location predictedDestination;
      private double directionX;
      private double directionZ;

      public PlayerMovementData() {
         this.recentLocations = new LinkedList<>();
      }

      public void addLocation(Location loc) {
         if (loc != null && loc.getWorld() != null) {
            this.recentLocations.offer(loc);
            if (this.recentLocations.size() > 50) {
               this.recentLocations.poll();
            }

            this.calculateMovementPattern();
         }
      }

      private void calculateMovementPattern() {
         if (this.recentLocations.size() >= 3) {
            List<Location> locs = new ArrayList<>(this.recentLocations);
            int size = locs.size();
            double avgDirX = 0.0;
            double avgDirZ = 0.0;

            for (int i = 1; i < size; i++) {
               Location prev = locs.get(i - 1);
               Location curr = locs.get(i);
               if (prev.getWorld() != null && curr.getWorld() != null && prev.getWorld().equals(curr.getWorld())) {
                  avgDirX += curr.getX() - prev.getX();
                  avgDirZ += curr.getZ() - prev.getZ();
               }
            }

            this.directionX = avgDirX / (size - 1);
            this.directionZ = avgDirZ / (size - 1);
            Location last = locs.get(size - 1);
            if (last.getWorld() != null) {
               double predictX = last.getX() + this.directionX * 10.0;
               double predictZ = last.getZ() + this.directionZ * 10.0;
               this.predictedDestination = new Location(last.getWorld(), predictX, last.getY(), predictZ);
            }
         }
      }

      public Set<SmartChunkUnloader.ChunkIdentifier> getPredictedChunkIds(int radius) {
         Set<SmartChunkUnloader.ChunkIdentifier> chunks = new HashSet<>();
         if (this.predictedDestination == null) {
            return chunks;
         } else {
            World world = this.predictedDestination.getWorld();
            if (world == null) {
               return chunks;
            } else {
               int centerX = this.predictedDestination.getBlockX() >> 4;
               int centerZ = this.predictedDestination.getBlockZ() >> 4;
               String worldName = world.getName();

               for (int x = -radius; x <= radius; x++) {
                  for (int z = -radius; z <= radius; z++) {
                     chunks.add(new SmartChunkUnloader.ChunkIdentifier(worldName, centerX + x, centerZ + z));
                  }
               }

               return chunks;
            }
         }
      }
   }
}

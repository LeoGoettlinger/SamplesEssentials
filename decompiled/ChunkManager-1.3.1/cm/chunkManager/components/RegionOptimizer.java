package cm.chunkManager.components;

import cm.chunkManager.utils.ChunkUtils;
import cm.chunkManager.utils.Scheduler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class RegionOptimizer {
   private final JavaPlugin plugin;
   private final Map<String, RegionOptimizer.RegionData> regions;
   private final Map<UUID, RegionOptimizer.PlayerRegionData> playerRegions;
   private final int regionSize = 8;

   public RegionOptimizer(JavaPlugin plugin) {
      this.plugin = plugin;
      this.regions = new ConcurrentHashMap<>();
      this.playerRegions = new ConcurrentHashMap<>();
   }

   public RegionOptimizer.RegionData getOrCreateRegion(World world, int chunkX, int chunkZ) {
      int regionX = Math.floorDiv(chunkX, 8);
      int regionZ = Math.floorDiv(chunkZ, 8);
      String key = world.getName() + "_" + regionX + "_" + regionZ;
      return this.regions.computeIfAbsent(key, k -> {
         RegionOptimizer.RegionData region = new RegionOptimizer.RegionData(world.getName(), regionX, regionZ);

         for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
               region.addChunk(regionX * 8 + x, regionZ * 8 + z);
            }
         }

         return region;
      });
   }

   public void onPlayerMove(Player player, Location from, Location to) {
      if (to != null && to.getWorld() != null) {
         int fromChunkX = from.getBlockX() >> 4;
         int fromChunkZ = from.getBlockZ() >> 4;
         int toChunkX = to.getBlockX() >> 4;
         int toChunkZ = to.getBlockZ() >> 4;
         if (fromChunkX != toChunkX || fromChunkZ != toChunkZ) {
            RegionOptimizer.RegionData newRegion = this.getOrCreateRegion(to.getWorld(), toChunkX, toChunkZ);
            RegionOptimizer.PlayerRegionData playerData = this.playerRegions
               .computeIfAbsent(player.getUniqueId(), k -> new RegionOptimizer.PlayerRegionData(player.getUniqueId()));
            String oldRegionKey = playerData.currentRegion;
            String newRegionKey = newRegion.getRegionKey();
            if (!newRegionKey.equals(oldRegionKey)) {
               if (oldRegionKey != null) {
                  RegionOptimizer.RegionData oldRegion = this.regions.get(oldRegionKey);
                  if (oldRegion != null) {
                     oldRegion.playerCount.updateAndGet(v -> Math.max(0, v - 1));
                  }
               }

               newRegion.playerCount.incrementAndGet();
            }

            newRegion.updateActivity(10);
            playerData.enterRegion(newRegionKey);
            this.preloadPredictedRegions(player, to.getWorld());
         }
      }
   }

   public void onPlayerQuit(Player player) {
      RegionOptimizer.PlayerRegionData data = this.playerRegions.remove(player.getUniqueId());
      if (data != null && data.currentRegion != null) {
         RegionOptimizer.RegionData region = this.regions.get(data.currentRegion);
         if (region != null) {
            region.playerCount.decrementAndGet();
         }
      }
   }

   private void preloadPredictedRegions(Player player, World world) {
      RegionOptimizer.PlayerRegionData playerData = this.playerRegions.get(player.getUniqueId());
      if (playerData != null) {
         for (String regionKey : playerData.getPredictedRegions()) {
            RegionOptimizer.RegionData region = this.regions.get(regionKey);
            if (region != null && !region.isOptimized) {
               this.optimizeRegion(world, region);
            }
         }
      }
   }

   public void optimizeRegion(World world, RegionOptimizer.RegionData region) {
      if (!region.isOptimized) {
         Scheduler.runAsync(this.plugin, () -> {
            for (RegionOptimizer.ChunkCoordinate coord : region.chunks) {
               if (region.isHighActivity && !world.isChunkLoaded(coord.x, coord.z)) {
                  ChunkUtils.getChunkAtAsync(world, coord.x, coord.z, this.plugin);
               }
            }

            region.isOptimized = true;
         });
      }
   }

   public void decayActivityScores() {
      long currentTime = System.currentTimeMillis();
      this.regions.values().forEach(region -> {
         if (currentTime - region.lastUpdate > 60000L) {
            int currentScore = region.activityScore.get();
            region.activityScore.set((int)(currentScore * 0.9));
            if (region.activityScore.get() < 10 && region.playerCount.get() == 0) {
               region.isOptimized = false;
            }
         }
      });
   }

   public List<RegionOptimizer.RegionData> getHighActivityRegions() {
      return this.regions
         .values()
         .stream()
         .filter(r -> r.isHighActivity)
         .sorted((a, b) -> Integer.compare(b.activityScore.get(), a.activityScore.get()))
         .collect(Collectors.toList());
   }

   public Map<String, Object> getRegionStatistics(World world) {
      Map<String, Object> stats = new HashMap<>();
      List<RegionOptimizer.RegionData> worldRegions = this.regions
         .values()
         .stream()
         .filter(r -> r.worldName.equals(world.getName()))
         .collect(Collectors.toList());
      int totalRegions = worldRegions.size();
      int activeRegions = (int)worldRegions.stream().filter(r -> r.playerCount.get() > 0).count();
      int optimizedRegions = (int)worldRegions.stream().filter(r -> r.isOptimized).count();
      int highActivityRegions = (int)worldRegions.stream().filter(r -> r.isHighActivity).count();
      stats.put("totalRegions", totalRegions);
      stats.put("activeRegions", activeRegions);
      stats.put("optimizedRegions", optimizedRegions);
      stats.put("highActivityRegions", highActivityRegions);
      return stats;
   }

   public void cleanup() {
      this.regions.clear();
      this.playerRegions.clear();
   }

   public static class ChunkCoordinate {
      public final int x;
      public final int z;

      public ChunkCoordinate(int x, int z) {
         this.x = x;
         this.z = z;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else {
            return !(o instanceof RegionOptimizer.ChunkCoordinate that) ? false : this.x == that.x && this.z == that.z;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.x, this.z);
      }
   }

   public static class PlayerRegionData {
      public final UUID playerId;
      public String currentRegion;
      public final List<String> recentRegions;
      public final Map<String, Integer> regionVisitCount;
      public long lastRegionChange;

      public PlayerRegionData(UUID playerId) {
         this.playerId = playerId;
         this.recentRegions = new ArrayList<>();
         this.regionVisitCount = new HashMap<>();
         this.lastRegionChange = System.currentTimeMillis();
      }

      public void enterRegion(String regionKey) {
         if (!regionKey.equals(this.currentRegion)) {
            if (this.currentRegion != null) {
               this.recentRegions.add(this.currentRegion);
               if (this.recentRegions.size() > 10) {
                  this.recentRegions.remove(0);
               }
            }

            this.currentRegion = regionKey;
            this.regionVisitCount.merge(regionKey, 1, Integer::sum);
            this.lastRegionChange = System.currentTimeMillis();
         }
      }

      public List<String> getPredictedRegions() {
         return this.regionVisitCount
            .entrySet()
            .stream()
            .sorted(Entry.<String, Integer>comparingByValue().reversed())
            .limit(3L)
            .map(Entry::getKey)
            .collect(Collectors.toList());
      }
   }

   public static class RegionData {
      public final String worldName;
      public final int regionX;
      public final int regionZ;
      public final Set<RegionOptimizer.ChunkCoordinate> chunks;
      public final AtomicInteger playerCount;
      public final AtomicInteger activityScore;
      public long lastUpdate;
      public boolean isHighActivity;
      public boolean isOptimized;

      public RegionData(String worldName, int regionX, int regionZ) {
         this.worldName = worldName;
         this.regionX = regionX;
         this.regionZ = regionZ;
         this.chunks = ConcurrentHashMap.newKeySet();
         this.playerCount = new AtomicInteger(0);
         this.activityScore = new AtomicInteger(0);
         this.lastUpdate = System.currentTimeMillis();
         this.isHighActivity = false;
         this.isOptimized = false;
      }

      public void addChunk(int chunkX, int chunkZ) {
         this.chunks.add(new RegionOptimizer.ChunkCoordinate(chunkX, chunkZ));
      }

      public void updateActivity(int delta) {
         this.activityScore.addAndGet(delta);
         this.lastUpdate = System.currentTimeMillis();
         this.isHighActivity = this.activityScore.get() > 100;
      }

      public String getRegionKey() {
         return this.worldName + "_" + this.regionX + "_" + this.regionZ;
      }
   }
}

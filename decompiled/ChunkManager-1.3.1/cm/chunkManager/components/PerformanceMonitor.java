package cm.chunkManager.components;

import cm.chunkManager.utils.Scheduler;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public class PerformanceMonitor {
   private final JavaPlugin plugin;
   private final Logger logger;
   private final DecimalFormat df = new DecimalFormat("#.##");
   private long lastTickTime = System.currentTimeMillis();
   private double averageTickTime = 50.0;
   private Consumer<PerformanceMonitor.PerformanceStatus> performanceHandler;

   public PerformanceMonitor(JavaPlugin plugin) {
      this.plugin = plugin;
      this.logger = plugin.getLogger();
   }

   public void setPerformanceHandler(Consumer<PerformanceMonitor.PerformanceStatus> handler) {
      this.performanceHandler = handler;
   }

   public void updateTickTime() {
      long currentTime = System.currentTimeMillis();
      long timeSpent = currentTime - this.lastTickTime;
      this.averageTickTime = (this.averageTickTime * 19.0 + timeSpent) / 20.0;
      this.lastTickTime = currentTime;
   }

   public PerformanceMonitor.PerformanceData getCurrentPerformance() {
      double tickTime = this.getAverageTickTime();
      MemoryUsage heapUsage = this.getHeapMemoryUsage();
      double memoryUsageMB = heapUsage.getUsed() / 1048576.0;
      double memoryPercentage = (double)heapUsage.getUsed() / heapUsage.getMax() * 100.0;
      int totalChunks = this.getTotalLoadedChunks();
      PerformanceMonitor.PerformanceStatus status = this.determinePerformanceStatus(tickTime, memoryPercentage);
      return new PerformanceMonitor.PerformanceData(tickTime, memoryUsageMB, memoryPercentage, totalChunks, status);
   }

   public PerformanceMonitor.PerformanceStatus checkPerformance(double tickThreshold, double memoryThreshold) {
      PerformanceMonitor.PerformanceData data = this.getCurrentPerformance();
      if (data.tickTime > tickThreshold || data.memoryPercentage > memoryThreshold) {
         return PerformanceMonitor.PerformanceStatus.POOR;
      } else {
         return data.tickTime < 40.0 && data.memoryPercentage < 60.0
            ? PerformanceMonitor.PerformanceStatus.EXCELLENT
            : PerformanceMonitor.PerformanceStatus.GOOD;
      }
   }

   private PerformanceMonitor.PerformanceStatus determinePerformanceStatus(double tickTime, double memoryPercentage) {
      if (tickTime > 70.0 || memoryPercentage > 85.0) {
         return PerformanceMonitor.PerformanceStatus.CRITICAL;
      } else if (tickTime > 55.0 || memoryPercentage > 75.0) {
         return PerformanceMonitor.PerformanceStatus.POOR;
      } else if (tickTime > 45.0 || memoryPercentage > 65.0) {
         return PerformanceMonitor.PerformanceStatus.MODERATE;
      } else {
         return !(tickTime > 35.0) && !(memoryPercentage > 50.0) ? PerformanceMonitor.PerformanceStatus.EXCELLENT : PerformanceMonitor.PerformanceStatus.GOOD;
      }
   }

   public void analyzePerformance() {
      PerformanceMonitor.PerformanceData data = this.getCurrentPerformance();
      if (this.performanceHandler != null) {
         this.performanceHandler.accept(data.status);
      }

      if (data.status == PerformanceMonitor.PerformanceStatus.CRITICAL) {
         this.logger
            .warning(
               "Critical performance detected! Tick time: " + this.df.format(data.tickTime) + "ms, Memory: " + this.df.format(data.memoryPercentage) + "%"
            );
      }
   }

   public PerformanceMonitor.WorldStatistics gatherWorldStatistics(World world) {
      PerformanceMonitor.WorldStatistics stats = new PerformanceMonitor.WorldStatistics();
      if (world == null) {
         return stats;
      } else {
         stats.totalPlayers = world.getPlayers().size();
         if (Scheduler.isFolia()) {
            return stats;
         } else {
            try {
               stats.totalLoadedEntities = world.getEntities().size();

               for (Chunk chunk : world.getLoadedChunks()) {
                  try {
                     int entitiesInChunk = chunk.getEntities().length;
                     int tileEntitiesInChunk = chunk.getTileEntities().length;
                     stats.totalEntities += entitiesInChunk;
                     stats.totalTileEntities += tileEntitiesInChunk;
                     if (entitiesInChunk > 0 || tileEntitiesInChunk > 0) {
                        stats.activeChunks++;
                     }
                  } catch (Exception var9) {
                  }
               }
            } catch (Exception var10) {
            }

            stats.totalTileEntitiesWorld = stats.totalTileEntities;
            return stats;
         }
      }
   }

   public Map<String, String> getPerformanceInfo() {
      PerformanceMonitor.PerformanceData data = this.getCurrentPerformance();
      Map<String, String> info = new HashMap<>();
      info.put("tick_time", this.df.format(data.tickTime));
      info.put("memory_mb", this.df.format(data.memoryUsage));
      info.put("memory_percent", this.df.format(data.memoryPercentage));
      info.put("total_chunks", String.valueOf(data.totalLoadedChunks));
      info.put("status", data.status.name());
      info.put("online_players", String.valueOf(Bukkit.getOnlinePlayers().size()));
      return info;
   }

   public double getAverageTickTime() {
      return this.averageTickTime;
   }

   public MemoryUsage getHeapMemoryUsage() {
      return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
   }

   public MemoryUsage getNonHeapMemoryUsage() {
      return ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();
   }

   public int getTotalLoadedChunks() {
      return Bukkit.getWorlds().stream().mapToInt(world -> world.getLoadedChunks().length).sum();
   }

   public void runGarbageCollection(Consumer<Map<String, Double>> callback) {
      double beforeGC = this.getHeapMemoryUsage().getUsed() / 1048576.0;
      System.gc();
      Scheduler.runGlobalLater(this.plugin, () -> {
         double afterGC = this.getHeapMemoryUsage().getUsed() / 1048576.0;
         double freedMemory = Math.abs(beforeGC - afterGC);
         Map<String, Double> results = new HashMap<>();
         results.put("before", beforeGC);
         results.put("after", afterGC);
         results.put("freed", freedMemory);
         callback.accept(results);
      }, 20L);
   }

   public static class PerformanceData {
      public final double tickTime;
      public final double memoryUsage;
      public final double memoryPercentage;
      public final int totalLoadedChunks;
      public final PerformanceMonitor.PerformanceStatus status;

      public PerformanceData(double tickTime, double memoryUsage, double memoryPercentage, int totalLoadedChunks, PerformanceMonitor.PerformanceStatus status) {
         this.tickTime = tickTime;
         this.memoryUsage = memoryUsage;
         this.memoryPercentage = memoryPercentage;
         this.totalLoadedChunks = totalLoadedChunks;
         this.status = status;
      }
   }

   public static enum PerformanceStatus {
      EXCELLENT,
      GOOD,
      MODERATE,
      POOR,
      CRITICAL;
   }

   public static class WorldStatistics {
      public int activeChunks;
      public int totalEntities;
      public int totalTileEntities;
      public int totalPlayers;
      public int totalLoadedEntities;
      public int totalTileEntitiesWorld;

      public Map<String, String> toPlaceholderMap() {
         Map<String, String> placeholders = new HashMap<>();
         placeholders.put("active_chunks", String.valueOf(this.activeChunks));
         placeholders.put("total_entities", String.valueOf(this.totalEntities));
         placeholders.put("total_tile_entities", String.valueOf(this.totalTileEntities));
         placeholders.put("total_players", String.valueOf(this.totalPlayers));
         placeholders.put("total_loaded_entities", String.valueOf(this.totalLoadedEntities));
         placeholders.put("total_tile_entities_world", String.valueOf(this.totalTileEntitiesWorld));
         return placeholders;
      }
   }
}

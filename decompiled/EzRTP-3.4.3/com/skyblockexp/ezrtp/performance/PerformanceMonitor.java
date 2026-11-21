package com.skyblockexp.ezrtp.performance;

import com.skyblockexp.ezrtp.config.performance.MetricsSettings;
import com.skyblockexp.ezrtp.config.performance.PerformanceSettings;
import com.skyblockexp.ezrtp.config.performance.WarningsSettings;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformTask;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.biome.BiomeLocationCache;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class PerformanceMonitor {
   private static final long TICKS_PER_MINUTE = 1200L;
   private final PerformanceSettings settings;
   private final RtpStatistics statistics;
   private final BiomeLocationCache biomeCache;
   private final Logger logger;
   private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "ezrtp-perf-io");
      t.setDaemon(true);
      return t;
   });
   private volatile PlatformTask periodicTask;

   public PerformanceMonitor(PerformanceSettings settings, RtpStatistics statistics, BiomeLocationCache biomeCache, Logger logger) {
      this.settings = settings;
      this.statistics = statistics;
      this.biomeCache = biomeCache;
      this.logger = logger;
   }

   public PerformanceSettings getSettings() {
      return this.settings;
   }

   public boolean isEnabled() {
      return this.settings.getMonitoring().isEnabled();
   }

   public void afterOperation(long durationMs, String worldName) {
      if (this.isEnabled()) {
         WarningsSettings warnings = this.settings.getWarnings();
         if (durationMs >= warnings.getSlowRtpThresholdMs()) {
            String message = String.format("Slow RTP: %dms in world '%s' (threshold %dms)", durationMs, worldName, warnings.getSlowRtpThresholdMs());
            if (warnings.isLogToConsole()) {
               this.logger.warning(message);
            }

            if (warnings.isLogToFile()) {
               String filePath = warnings.getFilePath();
               String line = Instant.now() + " " + message + "\n";
               this.ioExecutor.submit(() -> this.appendToFile(filePath, line));
            }
         }
      }
   }

   public void schedulePeriodicExport(PlatformScheduler scheduler) {
      this.cancelPeriodicTask();
      MetricsSettings metrics = this.settings.getMetrics();
      if (this.isEnabled() && metrics.isEnabled()) {
         long periodTicks = 1200L * metrics.getExportIntervalMinutes();
         this.periodicTask = scheduler.scheduleRepeating(this::exportNow, periodTicks, periodTicks);
      }
   }

   public void exportNow() {
      if (this.isEnabled()) {
         String json = this.snapshot().toJson();
         String filePath = this.settings.getMetrics().getExportPath();
         this.ioExecutor.submit(() -> this.writeFile(filePath, json));
      }
   }

   public PerformanceSnapshot snapshot() {
      long slowOpThreshold = this.settings.getWarnings().getSlowRtpThresholdMs();
      BiomeLocationCache.CacheStats cacheStats = this.biomeCache.getStats();
      return new PerformanceSnapshot(
         Instant.now().toString(),
         this.statistics.getTotalAttempts(),
         this.statistics.getTotalSuccesses(),
         this.statistics.getTotalFailures(),
         this.statistics.getSuccessRate(),
         this.statistics.getTimingPercentile(50),
         this.statistics.getTimingPercentile(75),
         this.statistics.getTimingPercentile(90),
         this.statistics.getTimingPercentile(95),
         this.statistics.getTimingPercentile(99),
         this.statistics.getMinTimeMs(),
         this.statistics.getMaxTimeMs(),
         this.statistics.getSlowOperationCount(slowOpThreshold),
         this.statistics.getCacheHitRate(),
         this.statistics.getTotalCacheHits(),
         this.statistics.getTotalCacheMisses(),
         cacheStats.evictions(),
         this.statistics.getTotalChunkLoads(),
         this.statistics.getAsyncChunkLoads(),
         this.statistics.getSyncChunkLoads(),
         this.statistics.getTotalBiomeRejections(),
         this.statistics.getMaxBiomeRejectionsPerRtp(),
         this.statistics.getBiomeFilterTimeouts()
      );
   }

   public void shutdown() {
      this.cancelPeriodicTask();
      this.ioExecutor.shutdown();

      try {
         if (!this.ioExecutor.awaitTermination(2L, TimeUnit.SECONDS)) {
            this.ioExecutor.shutdownNow();
         }
      } catch (InterruptedException var2) {
         this.ioExecutor.shutdownNow();
         Thread.currentThread().interrupt();
      }
   }

   private void cancelPeriodicTask() {
      PlatformTask task = this.periodicTask;
      if (task != null) {
         try {
            task.cancel();
         } catch (Exception var3) {
         }

         this.periodicTask = null;
      }
   }

   private void writeFile(String filePath, String content) {
      try {
         Path path = Paths.get(filePath);
         Path parent = path.getParent();
         if (parent != null) {
            Files.createDirectories(parent);
         }

         Files.writeString(path, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      } catch (IOException var5) {
         this.logger.warning("Failed to write performance metrics to '" + filePath + "': " + var5.getMessage());
      }
   }

   private void appendToFile(String filePath, String line) {
      try {
         Path path = Paths.get(filePath);
         Path parent = path.getParent();
         if (parent != null) {
            Files.createDirectories(parent);
         }

         Files.writeString(path, line, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
      } catch (IOException var5) {
         this.logger.warning("Failed to write slow-RTP warning to '" + filePath + "': " + var5.getMessage());
      }
   }
}

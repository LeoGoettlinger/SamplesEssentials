package com.skyblockexp.ezrtp.unsafe;

import com.skyblockexp.ezrtp.config.safety.LoggingSettings;
import com.skyblockexp.ezrtp.config.safety.MetricsSettings;
import com.skyblockexp.ezrtp.config.safety.UnsafeLocationSettings;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.platform.PlatformTask;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
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

public final class UnsafeLocationMonitor {
   private static final long TICKS_PER_MINUTE = 1200L;
   private final UnsafeLocationSettings settings;
   private final UnsafeLocationStatistics unsafeStats;
   private final RtpStatistics rtpStats;
   private final Logger logger;
   private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor(r -> {
      Thread t = new Thread(r, "ezrtp-unsafe-io");
      t.setDaemon(true);
      return t;
   });
   private volatile PlatformTask periodicTask;

   public UnsafeLocationMonitor(UnsafeLocationSettings settings, UnsafeLocationStatistics unsafeStats, RtpStatistics rtpStats, Logger logger) {
      this.settings = settings;
      this.unsafeStats = unsafeStats;
      this.rtpStats = rtpStats;
      this.logger = logger;
   }

   public UnsafeLocationSettings getSettings() {
      return this.settings;
   }

   public boolean isEnabled() {
      return this.settings.getMonitoring().isEnabled();
   }

   public void recordRejection(UnsafeLocationCause cause) {
      if (this.isEnabled() && this.settings.getCauses().isTrack()) {
         this.unsafeStats.recordRejection(cause);
      }
   }

   public UnsafeLocationStatistics getStatistics() {
      return this.unsafeStats;
   }

   public void schedulePeriodicExport(PlatformScheduler scheduler) {
      this.cancelPeriodicTask();
      MetricsSettings metricsSettings = this.settings.getMetrics();
      if (this.isEnabled() && metricsSettings.isEnabled()) {
         long periodTicks = 1200L * metricsSettings.getExportIntervalMinutes();
         this.periodicTask = scheduler.scheduleRepeating(this::exportNow, periodTicks, periodTicks);
      }
   }

   public void exportNow() {
      if (this.isEnabled()) {
         long currentAttempts = this.rtpStats.getTotalAttempts();
         boolean metricsEnabled = this.settings.getMetrics().isEnabled();
         UnsafeLocationStatistics.WindowSnapshot windowSnapshot = this.unsafeStats.snapshotWindow(currentAttempts, true);
         UnsafeLocationSnapshot snapshot = new UnsafeLocationSnapshot(
            Instant.now().toString(),
            this.settings.getMetrics().getExportIntervalMinutes(),
            windowSnapshot.windowAttempts(),
            windowSnapshot.totalUnsafe(),
            windowSnapshot.unsafeRate(),
            windowSnapshot.counts()
         );
         if (metricsEnabled) {
            String json = snapshot.toJson();
            String exportPath = this.settings.getMetrics().getExportPath();
            this.ioExecutor.submit(() -> this.writeFile(exportPath, json));
         }

         LoggingSettings logging = this.settings.getLogging();
         if (logging.isWarnOnUnsafe() && snapshot.getUnsafeRate() * 100.0 >= logging.getWarnThresholdPercent()) {
            String message = String.format(
               "High unsafe location rejection rate: %.1f%% (%d/%d RTPs) in the last %d minutes",
               snapshot.getUnsafeRate() * 100.0,
               snapshot.getUnsafeAttempts(),
               snapshot.getTotalAttempts(),
               this.settings.getMetrics().getExportIntervalMinutes()
            );
            if (logging.isLogToConsole()) {
               this.logger.warning(message);
            }

            if (logging.isLogToFile()) {
               String line = Instant.now() + " " + message + "\n";
               String filePath = logging.getFilePath();
               this.ioExecutor.submit(() -> this.appendToFile(filePath, line));
            }
         }
      }
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
         this.logger.warning("Failed to write unsafe-location metrics to '" + filePath + "': " + var5.getMessage());
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
         this.logger.warning("Failed to write unsafe-location warning to '" + filePath + "': " + var5.getMessage());
      }
   }
}

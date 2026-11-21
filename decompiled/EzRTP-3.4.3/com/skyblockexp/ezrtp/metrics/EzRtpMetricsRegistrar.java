package com.skyblockexp.ezrtp.metrics;

import com.skyblockexp.ezrtp.EzRtpPlugin;
import com.skyblockexp.ezrtp.libs.org.bstats.bukkit.Metrics;
import com.skyblockexp.ezrtp.libs.org.bstats.charts.SimplePie;
import com.skyblockexp.ezrtp.libs.org.bstats.charts.SingleLineChart;
import com.skyblockexp.ezrtp.statistics.RtpStatistics;
import com.skyblockexp.ezrtp.teleport.RandomTeleportService;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationCause;
import com.skyblockexp.ezrtp.unsafe.UnsafeLocationMonitor;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

public final class EzRtpMetricsRegistrar {
   private static final int BSTATS_PLUGIN_ID = 27735;
   private static final double[] TELEPORT_TIME_BUCKET_THRESHOLDS = new double[]{500.0, 2000.0, 5000.0, 10000.0, 30000.0};
   private static final String[] TELEPORT_TIME_BUCKET_LABELS = new String[]{"<500ms", "500-1999ms", "2000-4999ms", "5000-9999ms", "10000-29999ms", "30000ms+"};
   private final EzRtpPlugin plugin;
   private final Supplier<RandomTeleportService> teleportServiceSupplier;
   private Metrics metrics;

   public EzRtpMetricsRegistrar(EzRtpPlugin plugin, Supplier<RandomTeleportService> teleportServiceSupplier) {
      this.plugin = plugin;
      this.teleportServiceSupplier = teleportServiceSupplier;
   }

   public void register() {
      if (this.metrics == null) {
         boolean enableBstats = this.plugin.getConfig().getBoolean("enable-bstats", true);
         if (!enableBstats) {
            this.plugin.getLogger().info("bStats metrics disabled in config.");
         } else {
            try {
               this.metrics = new Metrics(this.plugin, 27735);
               this.metrics.addCustomChart(new SingleLineChart("cached_locations", () -> {
                  RandomTeleportService service = this.teleportServiceSupplier.get();
                  return service != null && service.getBiomeCache() != null ? service.getBiomeCache().getStats().locationCount() : 0;
               }));
               this.metrics.addCustomChart(new SingleLineChart("cache_hit_rate", () -> {
                  RandomTeleportService service = this.teleportServiceSupplier.get();
                  return service != null && service.getStatistics() != null ? (int)service.getStatistics().getCacheHitRate() : 0;
               }));
               this.metrics.addCustomChart(new SimplePie("rtp_success_distribution", this::describeSuccessRateBucket));
               this.metrics.addCustomChart(new SimplePie("teleport_time_distribution", this::describeAverageTeleportTimeBucket));
               this.metrics.addCustomChart(new SimplePie("failure_cause", this::describeTopFailureCause));
               this.metrics.addCustomChart(new SimplePie("unsafe_location_cause", this::describeTopUnsafeLocationCause));
               this.metrics.addCustomChart(new SimplePie("cache_enabled", () -> {
                  RandomTeleportService service = this.teleportServiceSupplier.get();
                  if (service != null && service.getBiomeCache() != null) {
                     return service.getBiomeCache().isEnabled() ? "Enabled" : "Disabled";
                  } else {
                     return "Unknown";
                  }
               }));
               this.metrics.addCustomChart(new SingleLineChart("total_rtps", () -> {
                  RandomTeleportService service = this.teleportServiceSupplier.get();
                  return service != null && service.getStatistics() != null ? service.getStatistics().getTotalAttempts() : 0;
               }));
               this.metrics.addCustomChart(new SingleLineChart("total_wait_time_seconds", () -> {
                  RandomTeleportService service = this.teleportServiceSupplier.get();
                  return service != null && service.getStatistics() != null ? (int)(service.getStatistics().getTotalTeleportTimeMs() / 1000L) : 0;
               }));
               this.plugin.getLogger().info("Metrics: bStats (https://bstats.org/plugin/bukkit/EzRTP/27735)");
            } catch (Throwable var3) {
               this.plugin.getLogger().warning("Failed to start bStats metrics: " + var3.getMessage());
            }
         }
      }
   }

   private String describeTopFailureCause() {
      RandomTeleportService service = this.teleportServiceSupplier.get();
      if (service != null && service.getStatistics() != null) {
         RtpStatistics.FailureCauses causes = service.getStatistics().getFailureCauses();
         if (causes.total() == 0) {
            return "No Failures";
         } else {
            Map<String, Integer> buckets = new LinkedHashMap<>();
            buckets.put("Safety", causes.safety());
            buckets.put("Biome", causes.biome());
            buckets.put("Protection", causes.protection());
            buckets.put("Timeout", causes.timeout());
            buckets.put("Economy", causes.economy());
            buckets.put("Offline/Cancelled", causes.playerOfflineOrCancelled());
            buckets.put("Teleport API", causes.teleportApi());
            buckets.put("Search Error", causes.genericSearchError());
            return buckets.entrySet()
               .stream()
               .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
               .max(Entry.comparingByValue())
               .map(Entry::getKey)
               .orElse("Unknown");
         }
      } else {
         return "No Data";
      }
   }

   private String describeTopUnsafeLocationCause() {
      UnsafeLocationMonitor monitor = this.plugin.getUnsafeLocationMonitor();
      if (monitor != null && monitor.isEnabled()) {
         Map<UnsafeLocationCause, Long> allTime = monitor.getStatistics().getAllTimeCounts();
         return allTime.entrySet()
            .stream()
            .filter(e -> e.getValue() > 0L)
            .max(Entry.comparingByValue())
            .map(e -> e.getKey().getDisplayName())
            .orElse("No Data");
      } else {
         return "No Data";
      }
   }

   private String describeSuccessRateBucket() {
      RandomTeleportService service = this.teleportServiceSupplier.get();
      if (service != null && service.getStatistics() != null) {
         double rate = service.getStatistics().getSuccessRate();
         if (Double.isNaN(rate)) {
            return "No Data";
         } else if (rate >= 95.0) {
            return "95-100%";
         } else if (rate >= 90.0) {
            return "90-94%";
         } else if (rate >= 80.0) {
            return "80-89%";
         } else if (rate >= 70.0) {
            return "70-79%";
         } else if (rate >= 60.0) {
            return "60-69%";
         } else {
            return rate >= 50.0 ? "50-59%" : "<50%";
         }
      } else {
         return "No Data";
      }
   }

   private String describeAverageTeleportTimeBucket() {
      RandomTeleportService service = this.teleportServiceSupplier.get();
      if (service != null && service.getStatistics() != null) {
         double avgTime = service.getStatistics().getAverageTeleportTimeMs();
         if (!Double.isNaN(avgTime) && !(avgTime < 0.0)) {
            for (int i = 0; i < TELEPORT_TIME_BUCKET_THRESHOLDS.length; i++) {
               if (avgTime < TELEPORT_TIME_BUCKET_THRESHOLDS[i]) {
                  return TELEPORT_TIME_BUCKET_LABELS[i];
               }
            }

            return TELEPORT_TIME_BUCKET_LABELS[TELEPORT_TIME_BUCKET_LABELS.length - 1];
         } else {
            return "No Data";
         }
      } else {
         return "No Data";
      }
   }
}

package com.skyblockexp.ezrtp.config.performance;

import java.util.List;
import org.bukkit.configuration.ConfigurationSection;

public final class PerformanceSettings {
   private static final int DEFAULT_SLOW_THRESHOLD_MS = 100;
   private static final String DEFAULT_FILE_PATH = "plugins/EzRTP/performance.log";
   private static final int DEFAULT_EXPORT_INTERVAL_MINUTES = 5;
   private static final String DEFAULT_EXPORT_PATH = "plugins/EzRTP/metrics.json";
   private static final List<Integer> DEFAULT_BUCKETS = List.of(50, 75, 90, 95, 99);
   private final MonitoringSettings monitoring;
   private final WarningsSettings warnings;
   private final MetricsSettings metrics;
   private final PercentilesSettings percentiles;

   private PerformanceSettings(MonitoringSettings monitoring, WarningsSettings warnings, MetricsSettings metrics, PercentilesSettings percentiles) {
      this.monitoring = monitoring;
      this.warnings = warnings;
      this.metrics = metrics;
      this.percentiles = percentiles;
   }

   public MonitoringSettings getMonitoring() {
      return this.monitoring;
   }

   public WarningsSettings getWarnings() {
      return this.warnings;
   }

   public MetricsSettings getMetrics() {
      return this.metrics;
   }

   public PercentilesSettings getPercentiles() {
      return this.percentiles;
   }

   public static PerformanceSettings defaults() {
      return new PerformanceSettings(
         new MonitoringSettings(false),
         new WarningsSettings(100, true, true, "plugins/EzRTP/performance.log"),
         new MetricsSettings(false, 5, "plugins/EzRTP/metrics.json"),
         new PercentilesSettings(true, DEFAULT_BUCKETS)
      );
   }

   public static PerformanceSettings fromConfiguration(ConfigurationSection root) {
      if (root == null) {
         return defaults();
      } else {
         ConfigurationSection perfSection = root.getConfigurationSection("performance");
         if (perfSection == null) {
            return defaults();
         } else {
            PerformanceSettings def = defaults();
            ConfigurationSection monSection = perfSection.getConfigurationSection("monitoring");
            boolean monEnabled = monSection != null ? monSection.getBoolean("enabled", false) : false;
            MonitoringSettings monitoring = new MonitoringSettings(monEnabled);
            ConfigurationSection warnSection = perfSection.getConfigurationSection("warnings");
            WarningsSettings warnings;
            if (warnSection != null) {
               int threshold = warnSection.getInt("slow-rtp-threshold-ms", def.getWarnings().getSlowRtpThresholdMs());
               boolean logConsole = warnSection.getBoolean("log-to-console", def.getWarnings().isLogToConsole());
               boolean logFile = warnSection.getBoolean("log-to-file", def.getWarnings().isLogToFile());
               String filePath = warnSection.getString("file-path", def.getWarnings().getFilePath());
               warnings = new WarningsSettings(threshold, logConsole, logFile, filePath);
            } else {
               warnings = def.getWarnings();
            }

            ConfigurationSection metSection = perfSection.getConfigurationSection("metrics");
            MetricsSettings metrics;
            if (metSection != null) {
               boolean metEnabled = metSection.getBoolean("enabled", def.getMetrics().isEnabled());
               int interval = metSection.getInt("export-interval-minutes", def.getMetrics().getExportIntervalMinutes());
               String exportPath = metSection.getString("export-path", def.getMetrics().getExportPath());
               metrics = new MetricsSettings(metEnabled, interval, exportPath);
            } else {
               metrics = def.getMetrics();
            }

            ConfigurationSection pctSection = perfSection.getConfigurationSection("percentiles");
            PercentilesSettings percentiles;
            if (pctSection != null) {
               boolean track = pctSection.getBoolean("track", def.getPercentiles().isTrack());
               List<Integer> buckets;
               if (pctSection.isList("buckets")) {
                  List<Integer> raw = pctSection.getIntegerList("buckets");
                  buckets = raw.isEmpty() ? def.getPercentiles().getBuckets() : List.copyOf(raw);
               } else {
                  buckets = def.getPercentiles().getBuckets();
               }

               percentiles = new PercentilesSettings(track, buckets);
            } else {
               percentiles = def.getPercentiles();
            }

            return new PerformanceSettings(monitoring, warnings, metrics, percentiles);
         }
      }
   }
}

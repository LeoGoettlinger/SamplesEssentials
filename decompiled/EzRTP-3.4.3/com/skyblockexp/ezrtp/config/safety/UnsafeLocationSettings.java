package com.skyblockexp.ezrtp.config.safety;

import org.bukkit.configuration.ConfigurationSection;

public final class UnsafeLocationSettings {
   private static final double DEFAULT_WARN_THRESHOLD_PERCENT = 20.0;
   private static final int DEFAULT_EXPORT_INTERVAL_MINUTES = 5;
   private static final String DEFAULT_LOG_FILE_PATH = "plugins/EzRTP/unsafe-locations.log";
   private static final String DEFAULT_EXPORT_PATH = "plugins/EzRTP/unsafe-metrics.json";
   private final MonitoringSettings monitoring;
   private final LoggingSettings logging;
   private final MetricsSettings metrics;
   private final CausesSettings causes;

   private UnsafeLocationSettings(MonitoringSettings monitoring, LoggingSettings logging, MetricsSettings metrics, CausesSettings causes) {
      this.monitoring = monitoring;
      this.logging = logging;
      this.metrics = metrics;
      this.causes = causes;
   }

   public MonitoringSettings getMonitoring() {
      return this.monitoring;
   }

   public LoggingSettings getLogging() {
      return this.logging;
   }

   public MetricsSettings getMetrics() {
      return this.metrics;
   }

   public CausesSettings getCauses() {
      return this.causes;
   }

   public static UnsafeLocationSettings defaults() {
      return new UnsafeLocationSettings(
         new MonitoringSettings(false),
         new LoggingSettings(true, true, true, "plugins/EzRTP/unsafe-locations.log", 20.0),
         new MetricsSettings(false, 5, "plugins/EzRTP/unsafe-metrics.json"),
         new CausesSettings(true)
      );
   }

   public static UnsafeLocationSettings fromConfiguration(ConfigurationSection root) {
      if (root == null) {
         return defaults();
      } else {
         ConfigurationSection section = root.getConfigurationSection("unsafe-location-monitoring");
         if (section == null) {
            return defaults();
         } else {
            UnsafeLocationSettings def = defaults();
            ConfigurationSection monSection = section.getConfigurationSection("monitoring");
            boolean monEnabled = monSection != null && monSection.getBoolean("enabled", false);
            MonitoringSettings monitoring = new MonitoringSettings(monEnabled);
            ConfigurationSection logSection = section.getConfigurationSection("logging");
            LoggingSettings logging;
            if (logSection != null) {
               boolean warnOnUnsafe = logSection.getBoolean("warn-on-unsafe", def.getLogging().isWarnOnUnsafe());
               boolean logToConsole = logSection.getBoolean("log-to-console", def.getLogging().isLogToConsole());
               boolean logToFile = logSection.getBoolean("log-to-file", def.getLogging().isLogToFile());
               String filePath = logSection.getString("file-path", def.getLogging().getFilePath());
               double warnThreshold = logSection.getDouble("warn-threshold-percent", def.getLogging().getWarnThresholdPercent());
               logging = new LoggingSettings(warnOnUnsafe, logToConsole, logToFile, filePath, warnThreshold);
            } else {
               logging = def.getLogging();
            }

            ConfigurationSection metSection = section.getConfigurationSection("metrics");
            MetricsSettings metrics;
            if (metSection != null) {
               boolean metEnabled = metSection.getBoolean("enabled", def.getMetrics().isEnabled());
               int interval = metSection.getInt("export-interval-minutes", def.getMetrics().getExportIntervalMinutes());
               String exportPath = metSection.getString("export-path", def.getMetrics().getExportPath());
               metrics = new MetricsSettings(metEnabled, interval, exportPath);
            } else {
               metrics = def.getMetrics();
            }

            ConfigurationSection causeSection = section.getConfigurationSection("causes");
            CausesSettings causes;
            if (causeSection != null) {
               boolean track = causeSection.getBoolean("track", def.getCauses().isTrack());
               causes = new CausesSettings(track);
            } else {
               causes = def.getCauses();
            }

            return new UnsafeLocationSettings(monitoring, logging, metrics, causes);
         }
      }
   }
}

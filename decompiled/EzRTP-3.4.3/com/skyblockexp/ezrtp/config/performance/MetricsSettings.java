package com.skyblockexp.ezrtp.config.performance;

public final class MetricsSettings {
   private static final int DEFAULT_EXPORT_INTERVAL_MINUTES = 5;
   private static final String DEFAULT_EXPORT_PATH = "plugins/EzRTP/metrics.json";
   private final boolean enabled;
   private final int exportIntervalMinutes;
   private final String exportPath;

   public MetricsSettings(boolean enabled, int exportIntervalMinutes, String exportPath) {
      this.enabled = enabled;
      this.exportIntervalMinutes = exportIntervalMinutes > 0 ? exportIntervalMinutes : 5;
      this.exportPath = exportPath != null ? exportPath : "plugins/EzRTP/metrics.json";
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public int getExportIntervalMinutes() {
      return this.exportIntervalMinutes;
   }

   public String getExportPath() {
      return this.exportPath;
   }
}

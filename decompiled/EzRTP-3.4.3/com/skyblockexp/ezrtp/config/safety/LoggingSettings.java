package com.skyblockexp.ezrtp.config.safety;

public final class LoggingSettings {
   private static final String DEFAULT_LOG_FILE_PATH = "plugins/EzRTP/unsafe-locations.log";
   private static final double DEFAULT_WARN_THRESHOLD_PERCENT = 20.0;
   private final boolean warnOnUnsafe;
   private final boolean logToConsole;
   private final boolean logToFile;
   private final String filePath;
   private final double warnThresholdPercent;

   public LoggingSettings(boolean warnOnUnsafe, boolean logToConsole, boolean logToFile, String filePath, double warnThresholdPercent) {
      this.warnOnUnsafe = warnOnUnsafe;
      this.logToConsole = logToConsole;
      this.logToFile = logToFile;
      this.filePath = filePath != null ? filePath : "plugins/EzRTP/unsafe-locations.log";
      this.warnThresholdPercent = warnThresholdPercent > 0.0 && warnThresholdPercent <= 100.0 ? warnThresholdPercent : 20.0;
   }

   public boolean isWarnOnUnsafe() {
      return this.warnOnUnsafe;
   }

   public boolean isLogToConsole() {
      return this.logToConsole;
   }

   public boolean isLogToFile() {
      return this.logToFile;
   }

   public String getFilePath() {
      return this.filePath;
   }

   public double getWarnThresholdPercent() {
      return this.warnThresholdPercent;
   }
}

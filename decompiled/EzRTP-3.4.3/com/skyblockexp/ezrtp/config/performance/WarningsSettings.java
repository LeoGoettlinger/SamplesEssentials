package com.skyblockexp.ezrtp.config.performance;

public final class WarningsSettings {
   private static final String DEFAULT_FILE_PATH = "plugins/EzRTP/performance.log";
   private final int slowRtpThresholdMs;
   private final boolean logToConsole;
   private final boolean logToFile;
   private final String filePath;

   public WarningsSettings(int slowRtpThresholdMs, boolean logToConsole, boolean logToFile, String filePath) {
      this.slowRtpThresholdMs = slowRtpThresholdMs;
      this.logToConsole = logToConsole;
      this.logToFile = logToFile;
      this.filePath = filePath != null ? filePath : "plugins/EzRTP/performance.log";
   }

   public int getSlowRtpThresholdMs() {
      return this.slowRtpThresholdMs;
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
}

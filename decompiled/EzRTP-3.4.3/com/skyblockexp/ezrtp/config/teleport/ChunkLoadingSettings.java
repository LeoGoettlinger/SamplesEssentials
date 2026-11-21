package com.skyblockexp.ezrtp.config.teleport;

import java.util.Locale;
import org.bukkit.configuration.ConfigurationSection;

public final class ChunkLoadingSettings {
   private final ChunkLoadingSettings.PaperAsyncApiMode usePaperAsyncApi;
   private final ChunkLoadingSettings.LegacyThrottleSettings legacyThrottle;

   public ChunkLoadingSettings(ChunkLoadingSettings.PaperAsyncApiMode usePaperAsyncApi, ChunkLoadingSettings.LegacyThrottleSettings legacyThrottle) {
      this.usePaperAsyncApi = usePaperAsyncApi != null ? usePaperAsyncApi : ChunkLoadingSettings.PaperAsyncApiMode.AUTO_DETECT;
      this.legacyThrottle = legacyThrottle != null ? legacyThrottle : ChunkLoadingSettings.LegacyThrottleSettings.defaults();
   }

   public ChunkLoadingSettings(boolean enabled, long processingIntervalTicks, int maxChunksPerTick) {
      this(
         ChunkLoadingSettings.PaperAsyncApiMode.AUTO_DETECT,
         new ChunkLoadingSettings.LegacyThrottleSettings(enabled, processingIntervalTicks, maxChunksPerTick)
      );
   }

   public ChunkLoadingSettings.PaperAsyncApiMode getUsePaperAsyncApi() {
      return this.usePaperAsyncApi;
   }

   public ChunkLoadingSettings.LegacyThrottleSettings getLegacyThrottle() {
      return this.legacyThrottle;
   }

   @Deprecated
   public boolean isEnabled() {
      return this.legacyThrottle.isEnabled();
   }

   @Deprecated
   public long getProcessingIntervalTicks() {
      return this.legacyThrottle.getProcessingIntervalTicks();
   }

   @Deprecated
   public int getMaxChunksPerTick() {
      return this.legacyThrottle.getMaxChunksPerTick();
   }

   public static ChunkLoadingSettings defaults() {
      return new ChunkLoadingSettings(ChunkLoadingSettings.PaperAsyncApiMode.AUTO_DETECT, ChunkLoadingSettings.LegacyThrottleSettings.defaults());
   }

   public static ChunkLoadingSettings fromConfiguration(ConfigurationSection section, ChunkLoadingSettings fallback) {
      ChunkLoadingSettings def = fallback != null ? fallback : defaults();
      if (section == null) {
         return def;
      } else {
         boolean hasNewKey = section.contains("use-paper-async-api") || section.isConfigurationSection("legacy-throttle");
         if (hasNewKey) {
            String modeStr = section.getString("use-paper-async-api", null);
            ChunkLoadingSettings.PaperAsyncApiMode mode = ChunkLoadingSettings.PaperAsyncApiMode.fromString(modeStr, def.getUsePaperAsyncApi());
            ConfigurationSection legacySection = section.getConfigurationSection("legacy-throttle");
            ChunkLoadingSettings.LegacyThrottleSettings legacy = ChunkLoadingSettings.LegacyThrottleSettings.fromSection(legacySection, def.getLegacyThrottle());
            return new ChunkLoadingSettings(mode, legacy);
         } else {
            boolean enabled = section.getBoolean("enabled", def.getLegacyThrottle().isEnabled());
            long interval = section.getLong("interval-ticks", def.getLegacyThrottle().getProcessingIntervalTicks());
            int maxChunks = section.getInt("max-chunks-per-tick", def.getLegacyThrottle().getMaxChunksPerTick());
            return new ChunkLoadingSettings(
               ChunkLoadingSettings.PaperAsyncApiMode.AUTO_DETECT, new ChunkLoadingSettings.LegacyThrottleSettings(enabled, interval, maxChunks)
            );
         }
      }
   }

   public static final class LegacyThrottleSettings {
      private final boolean enabled;
      private final long processingIntervalTicks;
      private final int maxChunksPerTick;

      public LegacyThrottleSettings(boolean enabled, long processingIntervalTicks, int maxChunksPerTick) {
         this.enabled = enabled;
         this.processingIntervalTicks = Math.max(1L, processingIntervalTicks);
         this.maxChunksPerTick = Math.max(1, maxChunksPerTick);
      }

      public boolean isEnabled() {
         return this.enabled;
      }

      public long getProcessingIntervalTicks() {
         return this.processingIntervalTicks;
      }

      public int getMaxChunksPerTick() {
         return this.maxChunksPerTick;
      }

      public static ChunkLoadingSettings.LegacyThrottleSettings defaults() {
         return new ChunkLoadingSettings.LegacyThrottleSettings(true, 3L, 2);
      }

      static ChunkLoadingSettings.LegacyThrottleSettings fromSection(ConfigurationSection section, ChunkLoadingSettings.LegacyThrottleSettings fallback) {
         ChunkLoadingSettings.LegacyThrottleSettings def = fallback != null ? fallback : defaults();
         if (section == null) {
            return def;
         } else {
            boolean enabled = section.getBoolean("enabled", def.isEnabled());
            long interval = section.getLong("interval-ticks", def.getProcessingIntervalTicks());
            int maxChunks = section.getInt("max-chunks-per-tick", def.getMaxChunksPerTick());
            return new ChunkLoadingSettings.LegacyThrottleSettings(enabled, interval, maxChunks);
         }
      }
   }

   public static enum PaperAsyncApiMode {
      AUTO_DETECT,
      ALWAYS,
      NEVER;

      public static ChunkLoadingSettings.PaperAsyncApiMode fromString(String value, ChunkLoadingSettings.PaperAsyncApiMode defaultMode) {
         if (value == null) {
            return defaultMode;
         } else {
            String var2 = value.toLowerCase(Locale.ROOT).trim();

            return switch (var2) {
               case "auto-detect", "auto" -> AUTO_DETECT;
               case "always", "true" -> ALWAYS;
               case "never", "false" -> NEVER;
               default -> defaultMode;
            };
         }
      }
   }
}

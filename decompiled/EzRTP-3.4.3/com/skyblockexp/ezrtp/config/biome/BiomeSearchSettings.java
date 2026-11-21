package com.skyblockexp.ezrtp.config.biome;

import org.bukkit.configuration.ConfigurationSection;

public final class BiomeSearchSettings {
   private final int maxWallClockMillis;
   private final int maxWallClockMillisRare;
   private final int maxBiomeRejections;
   private final int maxBiomeRejectionsRare;
   private final int maxChunkLoads;
   private final int maxChunkLoadsRare;
   private final int minBiomeAttempts;
   private final BiomeSearchSettings.FailoverMode failoverMode;
   private final boolean asyncMode;

   public BiomeSearchSettings(
      int maxWallClockMillis,
      int maxWallClockMillisRare,
      int maxBiomeRejections,
      int maxBiomeRejectionsRare,
      int maxChunkLoads,
      int maxChunkLoadsRare,
      int minBiomeAttempts,
      BiomeSearchSettings.FailoverMode failoverMode
   ) {
      this(
         maxWallClockMillis,
         maxWallClockMillisRare,
         maxBiomeRejections,
         maxBiomeRejectionsRare,
         maxChunkLoads,
         maxChunkLoadsRare,
         minBiomeAttempts,
         failoverMode,
         true
      );
   }

   public BiomeSearchSettings(
      int maxWallClockMillis,
      int maxWallClockMillisRare,
      int maxBiomeRejections,
      int maxBiomeRejectionsRare,
      int maxChunkLoads,
      int maxChunkLoadsRare,
      int minBiomeAttempts,
      BiomeSearchSettings.FailoverMode failoverMode,
      boolean asyncMode
   ) {
      this.maxWallClockMillis = Math.max(0, maxWallClockMillis);
      this.maxWallClockMillisRare = Math.max(0, maxWallClockMillisRare);
      this.maxBiomeRejections = Math.max(0, maxBiomeRejections);
      this.maxBiomeRejectionsRare = Math.max(0, maxBiomeRejectionsRare);
      this.maxChunkLoads = Math.max(0, maxChunkLoads);
      this.maxChunkLoadsRare = Math.max(0, maxChunkLoadsRare);
      this.minBiomeAttempts = Math.max(1, minBiomeAttempts);
      this.failoverMode = failoverMode != null ? failoverMode : BiomeSearchSettings.FailoverMode.CACHE;
      this.asyncMode = asyncMode;
   }

   public int getMaxWallClockMillis() {
      return this.maxWallClockMillis;
   }

   public int getMaxWallClockMillisRare() {
      return this.maxWallClockMillisRare;
   }

   public int getMaxBiomeRejections() {
      return this.maxBiomeRejections;
   }

   public int getMaxBiomeRejectionsRare() {
      return this.maxBiomeRejectionsRare;
   }

   public int getMaxChunkLoads() {
      return this.maxChunkLoads;
   }

   public int getMaxChunkLoadsRare() {
      return this.maxChunkLoadsRare;
   }

   public int getMinBiomeAttempts() {
      return this.minBiomeAttempts;
   }

   public BiomeSearchSettings.FailoverMode getFailoverMode() {
      return this.failoverMode;
   }

   public boolean isAsyncMode() {
      return this.asyncMode;
   }

   public static BiomeSearchSettings defaults() {
      return new BiomeSearchSettings(0, 5000, 0, 64, 0, 16, 96, BiomeSearchSettings.FailoverMode.CACHE, true);
   }

   public static BiomeSearchSettings filterAwareDefaults() {
      return new BiomeSearchSettings(100, 8000, 0, 64, 10, 16, 96, BiomeSearchSettings.FailoverMode.CACHE, true);
   }

   public static BiomeSearchSettings fromConfiguration(ConfigurationSection section, BiomeSearchSettings fallback) {
      if (section == null) {
         return fallback != null ? fallback : defaults();
      } else {
         BiomeSearchSettings base = fallback != null ? fallback : defaults();
         int maxWallClock = resolveWallClock(section, "max-wait-seconds", "max-total-time-ms", "max-wall-clock-millis", base.getMaxWallClockMillis());
         int maxWallClockRare = resolveWallClock(
            section, "max-wait-seconds-rare", "max-total-time-ms-rare", "max-wall-clock-millis-rare", base.getMaxWallClockMillisRare()
         );
         int maxBiomeRejections = Math.max(0, section.getInt("max-biome-rejections", base.getMaxBiomeRejections()));
         int maxBiomeRejectionsRare = Math.max(0, section.getInt("max-biome-rejections-rare", base.getMaxBiomeRejectionsRare()));
         int maxChunkLoads = Math.max(0, section.getInt("max-chunk-loads", base.getMaxChunkLoads()));
         int maxChunkLoadsRare = Math.max(0, section.getInt("max-chunk-loads-rare", base.getMaxChunkLoadsRare()));
         int minBiomeAttempts = Math.max(1, section.getInt("min-biome-attempts", base.getMinBiomeAttempts()));
         String failoverRaw = section.isSet("fallback-on-timeout") ? section.getString("fallback-on-timeout") : section.getString("failover-mode");
         BiomeSearchSettings.FailoverMode failoverMode = BiomeSearchSettings.FailoverMode.fromString(failoverRaw, base.getFailoverMode());
         boolean asyncMode = section.getBoolean("async-mode", base.isAsyncMode());
         return new BiomeSearchSettings(
            maxWallClock,
            maxWallClockRare,
            maxBiomeRejections,
            maxBiomeRejectionsRare,
            maxChunkLoads,
            maxChunkLoadsRare,
            minBiomeAttempts,
            failoverMode,
            asyncMode
         );
      }
   }

   private static int resolveWallClock(ConfigurationSection section, String secondsKey, String millisKey, String legacyMillisKey, int fallbackMillis) {
      if (section.isSet(secondsKey)) {
         int seconds = Math.max(0, section.getInt(secondsKey));
         return seconds * 1000;
      } else if (section.isSet(millisKey)) {
         return Math.max(0, section.getInt(millisKey));
      } else {
         return section.isSet(legacyMillisKey) ? Math.max(0, section.getInt(legacyMillisKey)) : Math.max(0, fallbackMillis);
      }
   }

   public static enum FailoverMode {
      CACHE,
      GENERIC,
      ABORT;

      public static BiomeSearchSettings.FailoverMode fromString(String value, BiomeSearchSettings.FailoverMode fallback) {
         if (value == null) {
            return fallback;
         } else {
            String normalized = value.trim().toUpperCase().replace('-', '_');
            if ("CACHED_LOCATION".equals(normalized)) {
               return CACHE;
            } else {
               try {
                  return valueOf(normalized);
               } catch (IllegalArgumentException var4) {
                  return fallback;
               }
            }
         }
      }
   }
}

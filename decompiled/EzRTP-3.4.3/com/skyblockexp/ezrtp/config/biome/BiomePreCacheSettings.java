package com.skyblockexp.ezrtp.config.biome;

import org.bukkit.configuration.ConfigurationSection;

public final class BiomePreCacheSettings {
   private final boolean enabled;
   private final int maxPerBiome;
   private final int warmupSize;
   private final long expirationMinutes;
   private final boolean autoEnableForFilters;
   private final int refillIntervalMinutes;
   private final int refillBatchSize;

   public BiomePreCacheSettings(
      boolean enabled, int maxPerBiome, int warmupSize, long expirationMinutes, boolean autoEnableForFilters, int refillIntervalMinutes, int refillBatchSize
   ) {
      this.enabled = enabled;
      this.maxPerBiome = Math.max(1, maxPerBiome);
      this.warmupSize = Math.max(0, warmupSize);
      this.expirationMinutes = Math.max(1L, expirationMinutes);
      this.autoEnableForFilters = autoEnableForFilters;
      this.refillIntervalMinutes = Math.max(1, refillIntervalMinutes);
      this.refillBatchSize = Math.max(1, refillBatchSize);
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public int getMaxPerBiome() {
      return this.maxPerBiome;
   }

   public int getWarmupSize() {
      return this.warmupSize;
   }

   public long getExpirationMinutes() {
      return this.expirationMinutes;
   }

   public boolean isAutoEnableForFilters() {
      return this.autoEnableForFilters;
   }

   public int getRefillIntervalMinutes() {
      return this.refillIntervalMinutes;
   }

   public int getRefillBatchSize() {
      return this.refillBatchSize;
   }

   public static BiomePreCacheSettings disabled() {
      return new BiomePreCacheSettings(false, 25, 5, 15L, true, 15, 2);
   }

   public static BiomePreCacheSettings fromConfiguration(ConfigurationSection section) {
      if (section == null) {
         return disabled();
      } else {
         boolean enabled = section.getBoolean("enabled", false);
         int maxPerBiome = section.getInt("max-per-biome", 25);
         int warmupSize = section.getInt("warmup-size", 5);
         long expirationMinutes = section.getLong("expiration-minutes", 15L);
         boolean autoEnableForFilters = section.getBoolean("auto-enable-for-filters", true);
         int refillIntervalMinutes = section.getInt("refill-interval-minutes", 15);
         int refillBatchSize = section.getInt("refill-batch-size", 2);
         return new BiomePreCacheSettings(enabled, maxPerBiome, warmupSize, expirationMinutes, autoEnableForFilters, refillIntervalMinutes, refillBatchSize);
      }
   }
}

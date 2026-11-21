package com.skyblockexp.ezrtp.config;

public final class ConfigurationDefaults {
   public static final int DEFAULT_COOLDOWN_SECONDS = 300;
   public static final int DEFAULT_DAILY_LIMIT = 10;
   public static final int DEFAULT_WEEKLY_LIMIT = 50;
   public static final int DEFAULT_MIN_RADIUS = 100;
   public static final int DEFAULT_MAX_RADIUS = 1000;
   public static final int DEFAULT_MAX_ATTEMPTS = 10;
   public static final int SAFETY_DEFAULT_MAX_SURFACE_SCAN_DEPTH = 20;
   public static final int SAFETY_DEFAULT_MAX_SURFACE_SCAN_DEPTH_NETHER = 128;
   public static final int SAFETY_MAX_SURFACE_SCAN_DEPTH_LIMIT = 128;
   public static final int SAFETY_DEFAULT_MAX_VERTICAL_RESCUE = 6;
   public static final int BIOME_SEARCH_DEFAULT_MAX_WALL_CLOCK_MILLIS = 0;
   public static final int BIOME_SEARCH_DEFAULT_MAX_WALL_CLOCK_MILLIS_RARE = 5000;
   public static final int BIOME_SEARCH_DEFAULT_MAX_REJECTIONS = 0;
   public static final int BIOME_SEARCH_DEFAULT_MAX_REJECTIONS_RARE = 64;
   public static final int BIOME_SEARCH_DEFAULT_MAX_CHUNK_LOADS = 0;
   public static final int BIOME_SEARCH_DEFAULT_MAX_CHUNK_LOADS_RARE = 16;
   public static final int BIOME_SEARCH_DEFAULT_MIN_BIOME_ATTEMPTS = 96;
   public static final int BIOME_SEARCH_FILTER_AWARE_MAX_WALL_CLOCK_MILLIS = 100;
   public static final int BIOME_SEARCH_FILTER_AWARE_MAX_CHUNK_LOADS = 10;
   public static final int BIOME_CACHE_DEFAULT_MAX_PER_BIOME = 25;
   public static final int BIOME_CACHE_DEFAULT_WARMUP_SIZE = 5;
   public static final long BIOME_CACHE_DEFAULT_EXPIRATION_MINUTES = 15L;
   public static final int BIOME_CACHE_DEFAULT_REFILL_INTERVAL_MINUTES = 15;
   public static final int BIOME_CACHE_DEFAULT_REFILL_BATCH_SIZE = 2;
   public static final long CHUNKY_DEFAULT_MIN_FREE_MEMORY_MB = 512L;
   public static final int CHUNKY_DEFAULT_MAX_COORDINATOR_ENTRIES = 10000;
   public static final long CHUNKY_DEFAULT_LOW_MEMORY_RETENTION_MINUTES = 15L;

   private ConfigurationDefaults() {
   }
}

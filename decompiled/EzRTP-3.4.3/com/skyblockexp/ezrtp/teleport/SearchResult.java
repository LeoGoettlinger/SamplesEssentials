package com.skyblockexp.ezrtp.teleport;

import java.util.Optional;
import org.bukkit.Location;

public record SearchResult(
   Optional<Location> location,
   boolean noValidBiome,
   boolean cacheHit,
   boolean cacheChecked,
   boolean fallbackUsed,
   boolean noCacheAvailable,
   SearchResult.SearchLimitType limitType
) {
   public SearchResult(Optional<Location> location, boolean noValidBiome, boolean cacheHit) {
      this(location, noValidBiome, cacheHit, cacheHit, false, false, SearchResult.SearchLimitType.NONE);
   }

   public SearchResult(Optional<Location> location, boolean noValidBiome, boolean cacheHit, boolean fallbackUsed) {
      this(location, noValidBiome, cacheHit, true, fallbackUsed, false, SearchResult.SearchLimitType.NONE);
   }

   public static enum SearchLimitType {
      NONE,
      WALL_CLOCK,
      BIOME_REJECTIONS,
      CHUNK_LOADS;
   }
}

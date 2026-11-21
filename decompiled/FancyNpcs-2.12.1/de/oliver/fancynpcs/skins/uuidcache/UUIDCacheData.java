package de.oliver.fancynpcs.skins.uuidcache;

import java.util.UUID;

public record UUIDCacheData(String username, UUID uuid, long lastUpdated, long timeToLive) {
   public boolean isExpired() {
      return this.timeToLive > 0L && System.currentTimeMillis() - this.lastUpdated > this.timeToLive;
   }
}

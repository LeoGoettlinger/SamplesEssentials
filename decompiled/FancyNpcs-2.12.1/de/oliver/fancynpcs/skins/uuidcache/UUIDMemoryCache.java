package de.oliver.fancynpcs.skins.uuidcache;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UUIDMemoryCache implements UUIDCache {
   private final Map<String, UUIDCacheData> cache = new ConcurrentHashMap<>();

   @Override
   public UUID getUUID(String username) {
      if (!this.cache.containsKey(username)) {
         return null;
      } else {
         UUIDCacheData data = this.cache.get(username);
         if (data.isExpired()) {
            this.cache.remove(username);
            return null;
         } else {
            return data.uuid();
         }
      }
   }

   @Override
   public void cacheUUID(String username, UUID uuid) {
      UUIDCacheData data = new UUIDCacheData(username, uuid, System.currentTimeMillis(), 2592000000L);
      this.cache.put(username, data);
   }

   @Override
   public void clearCache() {
      this.cache.clear();
   }
}

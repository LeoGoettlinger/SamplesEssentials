package de.oliver.fancynpcs.skins.uuidcache;

import java.util.UUID;

public interface UUIDCache {
   long CACHE_TIME = 2592000000L;

   UUID getUUID(String username);

   void cacheUUID(String username, UUID uuid);

   void clearCache();
}

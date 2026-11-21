package de.oliver.fancynpcs.skins.cache;

import de.oliver.fancynpcs.api.skins.SkinData;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkinCacheMemory implements SkinCache {
   private final Map<String, SkinCacheData> cache = new ConcurrentHashMap<>();

   @Override
   public SkinCacheData getSkin(String identifier) {
      if (!this.cache.containsKey(identifier)) {
         return null;
      } else {
         SkinCacheData skinCacheData = this.cache.get(identifier);
         if (skinCacheData.isExpired()) {
            this.cache.remove(identifier);
            return null;
         } else {
            return skinCacheData;
         }
      }
   }

   @Override
   public void addSkin(SkinData skin) {
      SkinCacheData skinCacheData = new SkinCacheData(skin, System.currentTimeMillis(), 604800000L);
      this.cache.put(skin.getParsedIdentifier(), skinCacheData);
   }

   @Override
   public void removeSkin(String identifier) {
      this.cache.remove(identifier);
   }

   @Override
   public void clear() {
      this.cache.clear();
   }
}

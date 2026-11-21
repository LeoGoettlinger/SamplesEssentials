package github.nighter.smartspawner.language.cache;

import github.nighter.smartspawner.utils.LRUCache;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class LanguageCache {
   private final LRUCache<String, String> stringCache = new LRUCache<>(1000);
   private final LRUCache<String, String[]> loreCache = new LRUCache<>(250);
   private final LRUCache<String, List<String>> loreListCache = new LRUCache<>(250);
   private final LRUCache<String, String> entityNameCache = new LRUCache<>(250);
   private final LRUCache<String, String> smallCapsCache = new LRUCache<>(500);
   private final LRUCache<String, String> materialNameCache = new LRUCache<>(250);
   private final AtomicInteger cacheHits;
   private final AtomicInteger cacheMisses;

   public LanguageCache(AtomicInteger cacheHits, AtomicInteger cacheMisses) {
      this.cacheHits = cacheHits;
      this.cacheMisses = cacheMisses;
   }

   public String getCachedString(String key) {
      String cached = this.stringCache.get(key);
      if (cached != null) {
         this.cacheHits.incrementAndGet();
         return cached;
      } else {
         this.cacheMisses.incrementAndGet();
         return null;
      }
   }

   public void putCachedString(String key, String value) {
      this.stringCache.put(key, value);
   }

   public String[] getCachedLore(String key) {
      String[] cached = this.loreCache.get(key);
      if (cached != null) {
         this.cacheHits.incrementAndGet();
         return cached;
      } else {
         this.cacheMisses.incrementAndGet();
         return null;
      }
   }

   public void putCachedLore(String key, String[] value) {
      this.loreCache.put(key, value);
   }

   public List<String> getCachedLoreList(String key) {
      List<String> cached = this.loreListCache.get(key);
      if (cached != null) {
         this.cacheHits.incrementAndGet();
         return cached;
      } else {
         this.cacheMisses.incrementAndGet();
         return null;
      }
   }

   public void putCachedLoreList(String key, List<String> value) {
      this.loreListCache.put(key, value);
   }

   public String getCachedEntityName(String key) {
      String cached = this.entityNameCache.get(key);
      if (cached != null) {
         this.cacheHits.incrementAndGet();
         return cached;
      } else {
         this.cacheMisses.incrementAndGet();
         return null;
      }
   }

   public void putCachedEntityName(String key, String value) {
      this.entityNameCache.put(key, value);
   }

   public String getCachedMaterialName(String key) {
      String cached = this.materialNameCache.get(key);
      if (cached != null) {
         this.cacheHits.incrementAndGet();
         return cached;
      } else {
         this.cacheMisses.incrementAndGet();
         return null;
      }
   }

   public void putCachedMaterialName(String key, String value) {
      this.materialNameCache.put(key, value);
   }

   public String getCachedSmallCaps(String key) {
      String cached = this.smallCapsCache.get(key);
      if (cached != null) {
         this.cacheHits.incrementAndGet();
         return cached;
      } else {
         this.cacheMisses.incrementAndGet();
         return null;
      }
   }

   public void putCachedSmallCaps(String key, String value) {
      this.smallCapsCache.put(key, value);
   }

   public void clear() {
      this.stringCache.clear();
      this.loreCache.clear();
      this.loreListCache.clear();
      this.entityNameCache.clear();
      this.smallCapsCache.clear();
      this.materialNameCache.clear();
   }

   public int getStringCacheSize() {
      return this.stringCache.size();
   }

   public int getStringCacheCapacity() {
      return this.stringCache.capacity();
   }

   public int getLoreCacheSize() {
      return this.loreCache.size();
   }

   public int getLoreCacheCapacity() {
      return this.loreCache.capacity();
   }

   public int getLoreListCacheSize() {
      return this.loreListCache.size();
   }

   public int getLoreListCacheCapacity() {
      return this.loreListCache.capacity();
   }

   public int getEntityNameCacheSize() {
      return this.entityNameCache.size();
   }

   public int getEntityNameCacheCapacity() {
      return this.entityNameCache.capacity();
   }

   public int getSmallCapsCacheSize() {
      return this.smallCapsCache.size();
   }

   public int getSmallCapsCacheCapacity() {
      return this.smallCapsCache.capacity();
   }

   public int getMaterialNameCacheSize() {
      return this.materialNameCache.size();
   }

   public int getMaterialNameCacheCapacity() {
      return this.materialNameCache.capacity();
   }
}

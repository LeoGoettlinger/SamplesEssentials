package org.sethomegui.SetHomeGUI.libs.redis.jedis.csc;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.annots.Experimental;
import org.sethomegui.SetHomeGUI.libs.redis.jedis.util.SafeEncoder;

@Experimental
public abstract class AbstractCache implements Cache {
   private Cacheable cacheable;
   private final Map<ByteBuffer, Set<CacheKey<?>>> redisKeysToCacheKeys = new ConcurrentHashMap<>();
   private final int maximumSize;
   private ReentrantLock lock = new ReentrantLock();
   private volatile CacheStats stats = new CacheStats();

   protected AbstractCache(int maximumSize) {
      this(maximumSize, DefaultCacheable.INSTANCE);
   }

   protected AbstractCache(int maximumSize, Cacheable cacheable) {
      this.maximumSize = maximumSize;
      this.cacheable = cacheable;
   }

   @Override
   public int getMaxSize() {
      return this.maximumSize;
   }

   @Override
   public abstract int getSize();

   @Override
   public abstract Collection<CacheEntry> getCacheEntries();

   @Override
   public CacheEntry get(CacheKey cacheKey) {
      CacheEntry entry = this.getFromStore(cacheKey);
      if (entry != null) {
         this.getEvictionPolicy().touch(cacheKey);
      }

      return entry;
   }

   @Override
   public CacheEntry set(CacheKey cacheKey, CacheEntry entry) {
      this.lock.lock();

      CacheEntry var13;
      try {
         entry = this.putIntoStore(cacheKey, entry);
         EvictionPolicy policy = this.getEvictionPolicy();
         policy.touch(cacheKey);
         CacheKey evictedKey = policy.evictNext();
         if (evictedKey != null) {
            this.delete(evictedKey);
            this.stats.evict();
         }

         for (Object redisKey : cacheKey.getRedisKeys()) {
            ByteBuffer mapKey = this.makeKeyForRedisKeysToCacheKeys(redisKey);
            if (this.redisKeysToCacheKeys.containsKey(mapKey)) {
               this.redisKeysToCacheKeys.get(mapKey).add(cacheKey);
            } else {
               Set<CacheKey<?>> set = ConcurrentHashMap.newKeySet();
               set.add(cacheKey);
               this.redisKeysToCacheKeys.put(mapKey, set);
            }
         }

         this.stats.load();
         var13 = entry;
      } finally {
         this.lock.unlock();
      }

      return var13;
   }

   @Override
   public boolean delete(CacheKey cacheKey) {
      this.lock.lock();

      boolean var10;
      try {
         boolean removed = this.removeFromStore(cacheKey);
         this.getEvictionPolicy().reset(cacheKey);

         for (Object redisKey : cacheKey.getRedisKeys()) {
            ByteBuffer mapKey = this.makeKeyForRedisKeysToCacheKeys(redisKey);
            Set<CacheKey<?>> cacheKeysRelatedtoRedisKey = this.redisKeysToCacheKeys.get(mapKey);
            if (cacheKeysRelatedtoRedisKey != null) {
               cacheKeysRelatedtoRedisKey.remove(cacheKey);
            }
         }

         var10 = removed;
      } finally {
         this.lock.unlock();
      }

      return var10;
   }

   @Override
   public List<Boolean> delete(List<CacheKey> cacheKeys) {
      this.lock.lock();

      List var2;
      try {
         var2 = cacheKeys.stream().map(this::delete).collect(Collectors.toList());
      } finally {
         this.lock.unlock();
      }

      return var2;
   }

   @Override
   public List<CacheKey> deleteByRedisKey(Object key) {
      this.lock.lock();

      Object var5;
      try {
         ByteBuffer mapKey = this.makeKeyForRedisKeysToCacheKeys(key);
         Set<CacheKey<?>> commands = this.redisKeysToCacheKeys.get(mapKey);
         List<CacheKey> cacheKeys = new ArrayList<>();
         if (commands != null) {
            cacheKeys.addAll(commands.stream().filter(this::removeFromStore).collect(Collectors.toList()));
            this.stats.invalidationByServer(cacheKeys.size());
            this.redisKeysToCacheKeys.remove(mapKey);
         }

         this.stats.invalidationMessages();
         var5 = cacheKeys;
      } finally {
         this.lock.unlock();
      }

      return (List<CacheKey>)var5;
   }

   @Override
   public List<CacheKey> deleteByRedisKeys(List keys) {
      if (keys == null) {
         this.flush();
         return null;
      } else {
         this.lock.lock();

         List var2;
         try {
            var2 = keys.stream().map(this::deleteByRedisKey).flatMap(Collection::stream).collect(Collectors.toList());
         } finally {
            this.lock.unlock();
         }

         return var2;
      }
   }

   @Override
   public int flush() {
      this.lock.lock();

      int var2;
      try {
         int result = this.getSize();
         this.clearStore();
         this.redisKeysToCacheKeys.clear();
         this.getEvictionPolicy().resetAll();
         this.getStats().flush();
         var2 = result;
      } finally {
         this.lock.unlock();
      }

      return var2;
   }

   @Override
   public boolean isCacheable(CacheKey cacheKey) {
      return this.cacheable.isCacheable(cacheKey.getRedisCommand(), cacheKey.getRedisKeys());
   }

   @Override
   public boolean hasCacheKey(CacheKey cacheKey) {
      return this.containsKeyInStore(cacheKey);
   }

   @Override
   public abstract EvictionPolicy getEvictionPolicy();

   @Override
   public CacheStats getStats() {
      return this.stats;
   }

   @Override
   public CacheStats getAndResetStats() {
      CacheStats result = this.stats;
      this.stats = new CacheStats();
      return result;
   }

   @Override
   public boolean compatibilityMode() {
      return false;
   }

   protected abstract CacheEntry getFromStore(CacheKey var1);

   protected abstract CacheEntry putIntoStore(CacheKey var1, CacheEntry var2);

   protected abstract boolean removeFromStore(CacheKey var1);

   protected abstract void clearStore();

   protected abstract boolean containsKeyInStore(CacheKey var1);

   private ByteBuffer makeKeyForRedisKeysToCacheKeys(Object key) {
      if (key instanceof byte[]) {
         return makeKeyForRedisKeysToCacheKeys((byte[])key);
      } else if (key instanceof String) {
         return makeKeyForRedisKeysToCacheKeys(SafeEncoder.encode((String)key));
      } else {
         throw new IllegalArgumentException(key.getClass().getSimpleName() + " is not supported. Value: \"" + key + "\".");
      }
   }

   private static ByteBuffer makeKeyForRedisKeysToCacheKeys(byte[] b) {
      return ByteBuffer.wrap(b);
   }
}

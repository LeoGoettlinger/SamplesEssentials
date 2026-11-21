package com.mysql.cj;

import com.mysql.cj.util.LRUCache;
import java.util.Set;
import java.util.concurrent.locks.Lock;

public class PerConnectionLRUFactory implements CacheAdapterFactory<String, QueryInfo> {
   @Override
   public CacheAdapter<String, QueryInfo> getInstance(Lock lock, String url, int cacheMaxSize, int maxKeySize) {
      return new PerConnectionLRUFactory.PerConnectionLRU(lock, cacheMaxSize, maxKeySize);
   }

   class PerConnectionLRU implements CacheAdapter<String, QueryInfo> {
      private final int cacheSqlLimit;
      private final LRUCache<String, QueryInfo> cache;
      private final Lock lock;

      protected PerConnectionLRU(Lock lock, int cacheMaxSize, int maxKeySize) {
         this.cacheSqlLimit = maxKeySize;
         this.cache = new LRUCache<>(cacheMaxSize);
         this.lock = lock;
      }

      public QueryInfo get(String key) {
         if (key != null && key.length() <= this.cacheSqlLimit) {
            this.lock.lock();

            QueryInfo var2;
            try {
               var2 = this.cache.get(key);
            } finally {
               this.lock.unlock();
            }

            return var2;
         } else {
            return null;
         }
      }

      public void put(String key, QueryInfo value) {
         if (key != null && key.length() <= this.cacheSqlLimit) {
            this.lock.lock();

            try {
               this.cache.put(key, value);
            } finally {
               this.lock.unlock();
            }
         }
      }

      public void invalidate(String key) {
         this.lock.lock();

         try {
            this.cache.remove(key);
         } finally {
            this.lock.unlock();
         }
      }

      @Override
      public void invalidateAll(Set<String> keys) {
         this.lock.lock();

         try {
            for (String key : keys) {
               this.cache.remove(key);
            }
         } finally {
            this.lock.unlock();
         }
      }

      @Override
      public void invalidateAll() {
         this.lock.lock();

         try {
            this.cache.clear();
         } finally {
            this.lock.unlock();
         }
      }
   }
}

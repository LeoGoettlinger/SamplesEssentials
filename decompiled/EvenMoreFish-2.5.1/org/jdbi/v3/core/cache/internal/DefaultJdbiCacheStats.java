package org.jdbi.v3.core.cache.internal;

public final class DefaultJdbiCacheStats {
   private final int cacheSize;
   private final int maxSize;

   DefaultJdbiCacheStats(int cacheSize, int maxSize) {
      this.cacheSize = cacheSize;
      this.maxSize = maxSize;
   }

   public int cacheSize() {
      return this.cacheSize;
   }

   public int maxSize() {
      return this.maxSize;
   }
}

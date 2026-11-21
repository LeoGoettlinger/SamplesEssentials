package org.jdbi.v3.core.cache.internal;

import org.jdbi.v3.core.cache.JdbiCache;
import org.jdbi.v3.core.cache.JdbiCacheBuilder;
import org.jdbi.v3.core.cache.JdbiCacheLoader;

public final class DefaultJdbiCacheBuilder implements JdbiCacheBuilder {
   private int maxSize = -1;

   public static DefaultJdbiCacheBuilder builder() {
      return new DefaultJdbiCacheBuilder();
   }

   private DefaultJdbiCacheBuilder() {
   }

   @Override
   public <K, V> JdbiCache<K, V> build() {
      return new DefaultJdbiCache<>(this, null);
   }

   @Override
   public <K, V> JdbiCache<K, V> buildWithLoader(JdbiCacheLoader<K, V> loader) {
      return new DefaultJdbiCache<>(this, loader);
   }

   public DefaultJdbiCacheBuilder maxSize(int maxSize) {
      this.maxSize = maxSize;
      return this;
   }

   int getMaxSize() {
      return this.maxSize;
   }
}

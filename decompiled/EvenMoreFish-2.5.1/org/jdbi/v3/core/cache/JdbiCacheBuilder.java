package org.jdbi.v3.core.cache;

public interface JdbiCacheBuilder {
   <K, V> JdbiCache<K, V> build();

   <K, V> JdbiCache<K, V> buildWithLoader(JdbiCacheLoader<K, V> cacheLoader);

   JdbiCacheBuilder maxSize(int maxSize);
}

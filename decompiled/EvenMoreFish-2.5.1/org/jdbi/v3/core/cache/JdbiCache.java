package org.jdbi.v3.core.cache;

public interface JdbiCache<K, V> {
   V get(K key);

   V getWithLoader(K key, JdbiCacheLoader<K, V> loader);

   <T> T getStats();
}

package org.jdbi.v3.core.cache;

@FunctionalInterface
public interface JdbiCacheLoader<K, V> {
   V create(K key);
}

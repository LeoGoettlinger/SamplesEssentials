package github.nighter.smartspawner.utils;

import com.google.common.base.Preconditions;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;

public class LRUCache<K, V> {
   private final LinkedHashMap<K, V> cache;
   private int capacity;

   public LRUCache(int capacity) {
      this.capacity = capacity;
      this.cache = new LinkedHashMap<K, V>(capacity, 0.75F, true) {
         {
            Objects.requireNonNull(LRUCache.this);
         }

         @Override
         protected boolean removeEldestEntry(Entry<K, V> eldest) {
            return this.size() > LRUCache.this.capacity;
         }
      };
   }

   public synchronized V get(K key) {
      return this.cache.get(key);
   }

   public synchronized V put(K key, V value) {
      return this.cache.put(key, value);
   }

   public synchronized V get(K key, Function<K, V> mappingFunction) {
      Preconditions.checkNotNull(key);
      return this.cache.computeIfAbsent(key, mappingFunction);
   }

   public synchronized void clear() {
      this.cache.clear();
   }

   public synchronized int size() {
      return this.cache.size();
   }

   public synchronized int capacity() {
      return this.capacity;
   }

   public synchronized void resize(int newCapacity) {
      this.capacity = newCapacity;
   }
}

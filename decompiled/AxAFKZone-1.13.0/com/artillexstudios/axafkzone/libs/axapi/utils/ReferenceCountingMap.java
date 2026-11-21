package com.artillexstudios.axafkzone.libs.axapi.utils;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;

public final class ReferenceCountingMap<K, V> extends ConcurrentHashMap<K, V> {
   private final Object2IntMap<Object> counter = new Object2IntOpenHashMap();

   @Override
   public V put(@NonNull K key, @NonNull V value) {
      synchronized (this.counter) {
         this.counter.compute(key, (k, v) -> v == null ? 1 : v + 1);
      }

      return super.put(key, value);
   }

   @Override
   public V remove(@NonNull Object key) {
      synchronized (this.counter) {
         int value = this.counter.getOrDefault(key, -1);
         if (value == -1) {
            return null;
         } else {
            V val;
            if (value - 1 == 0) {
               this.counter.removeInt(key);
               val = super.remove(key);
            } else {
               value--;
               val = super.get(key);
            }

            this.counter.put(key, value);
            return val;
         }
      }
   }
}

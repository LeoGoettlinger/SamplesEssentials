package com.viaversion.viaversion.libs.fastutil.longs;

import com.viaversion.viaversion.libs.fastutil.HashCommon;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectMaps.SynchronizedMap;
import com.viaversion.viaversion.libs.fastutil.longs.Long2ObjectMaps.UnmodifiableMap;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectCollection;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterable;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectIterator;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectSet;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectSets;
import com.viaversion.viaversion.libs.fastutil.objects.ObjectSpliterator;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class Long2ObjectMaps {
   public static final Long2ObjectMaps.EmptyMap EMPTY_MAP = new Long2ObjectMaps.EmptyMap();

   private Long2ObjectMaps() {
   }

   public static <V> ObjectIterator<Long2ObjectMap.Entry<V>> fastIterator(Long2ObjectMap<V> map) {
      ObjectSet<Long2ObjectMap.Entry<V>> entries = map.long2ObjectEntrySet();
      return entries instanceof Long2ObjectMap.FastEntrySet ? ((Long2ObjectMap.FastEntrySet)entries).fastIterator() : entries.iterator();
   }

   public static <V> void fastForEach(Long2ObjectMap<V> map, Consumer<? super Long2ObjectMap.Entry<V>> consumer) {
      ObjectSet<Long2ObjectMap.Entry<V>> entries = map.long2ObjectEntrySet();
      if (entries instanceof Long2ObjectMap.FastEntrySet) {
         ((Long2ObjectMap.FastEntrySet)entries).fastForEach(consumer);
      } else {
         entries.forEach(consumer);
      }
   }

   public static <V> ObjectIterable<Long2ObjectMap.Entry<V>> fastIterable(Long2ObjectMap<V> map) {
      final ObjectSet<Long2ObjectMap.Entry<V>> entries = map.long2ObjectEntrySet();
      return (ObjectIterable<Long2ObjectMap.Entry<V>>)(entries instanceof Long2ObjectMap.FastEntrySet ? new ObjectIterable<Long2ObjectMap.Entry<V>>() {
         @Override
         public ObjectIterator<Long2ObjectMap.Entry<V>> iterator() {
            return ((Long2ObjectMap.FastEntrySet)entries).fastIterator();
         }

         @Override
         public ObjectSpliterator<Long2ObjectMap.Entry<V>> spliterator() {
            return entries.spliterator();
         }

         @Override
         public void forEach(Consumer<? super Long2ObjectMap.Entry<V>> consumer) {
            ((Long2ObjectMap.FastEntrySet)entries).fastForEach(consumer);
         }
      } : entries);
   }

   public static <V> Long2ObjectMap<V> emptyMap() {
      return EMPTY_MAP;
   }

   public static <V> Long2ObjectMap<V> singleton(long key, V value) {
      return new Long2ObjectMaps.Singleton<>(key, value);
   }

   public static <V> Long2ObjectMap<V> singleton(Long key, V value) {
      return new Long2ObjectMaps.Singleton<>(key, value);
   }

   public static <V> Long2ObjectMap<V> synchronize(Long2ObjectMap<V> m) {
      return new SynchronizedMap(m);
   }

   public static <V> Long2ObjectMap<V> synchronize(Long2ObjectMap<V> m, Object sync) {
      return new SynchronizedMap(m, sync);
   }

   public static <V> Long2ObjectMap<V> unmodifiable(Long2ObjectMap<? extends V> m) {
      return new UnmodifiableMap(m);
   }

   public static class EmptyMap<V> extends Long2ObjectFunctions.EmptyFunction<V> implements Long2ObjectMap<V>, Serializable, Cloneable {
      private static final long serialVersionUID = -7046029254386353129L;

      protected EmptyMap() {
      }

      @Override
      public boolean containsValue(Object v) {
         return false;
      }

      @Deprecated
      @Override
      public V getOrDefault(Object key, V defaultValue) {
         return defaultValue;
      }

      @Override
      public V getOrDefault(long key, V defaultValue) {
         return defaultValue;
      }

      @Override
      public void putAll(Map<? extends Long, ? extends V> m) {
         throw new UnsupportedOperationException();
      }

      @Override
      public ObjectSet<Long2ObjectMap.Entry<V>> long2ObjectEntrySet() {
         return ObjectSets.EMPTY_SET;
      }

      @Override
      public LongSet keySet() {
         return LongSets.EMPTY_SET;
      }

      @Override
      public ObjectCollection<V> values() {
         return ObjectSets.EMPTY_SET;
      }

      @Override
      public void forEach(BiConsumer<? super Long, ? super V> consumer) {
      }

      @Override
      public Object clone() {
         return Long2ObjectMaps.EMPTY_MAP;
      }

      @Override
      public boolean isEmpty() {
         return true;
      }

      @Override
      public int hashCode() {
         return 0;
      }

      @Override
      public boolean equals(Object o) {
         return !(o instanceof Map) ? false : ((Map)o).isEmpty();
      }

      @Override
      public String toString() {
         return "{}";
      }
   }

   public static class Singleton<V> extends Long2ObjectFunctions.Singleton<V> implements Long2ObjectMap<V>, Serializable, Cloneable {
      private static final long serialVersionUID = -7046029254386353129L;
      protected transient ObjectSet<Long2ObjectMap.Entry<V>> entries;
      protected transient LongSet keys;
      protected transient ObjectCollection<V> values;

      protected Singleton(long key, V value) {
         super(key, value);
      }

      @Override
      public boolean containsValue(Object v) {
         return Objects.equals(this.value, v);
      }

      @Override
      public void putAll(Map<? extends Long, ? extends V> m) {
         throw new UnsupportedOperationException();
      }

      @Override
      public ObjectSet<Long2ObjectMap.Entry<V>> long2ObjectEntrySet() {
         if (this.entries == null) {
            this.entries = ObjectSets.singleton(new AbstractLong2ObjectMap.BasicEntry<>(this.key, this.value));
         }

         return this.entries;
      }

      @Deprecated
      @Override
      public ObjectSet<Entry<Long, V>> entrySet() {
         return this.long2ObjectEntrySet();
      }

      @Override
      public LongSet keySet() {
         if (this.keys == null) {
            this.keys = LongSets.singleton(this.key);
         }

         return this.keys;
      }

      @Override
      public ObjectCollection<V> values() {
         if (this.values == null) {
            this.values = ObjectSets.singleton(this.value);
         }

         return this.values;
      }

      @Override
      public boolean isEmpty() {
         return false;
      }

      @Override
      public int hashCode() {
         return HashCommon.long2int(this.key) ^ (this.value == null ? 0 : this.value.hashCode());
      }

      @Override
      public boolean equals(Object o) {
         if (o == this) {
            return true;
         } else if (!(o instanceof Map)) {
            return false;
         } else {
            Map<?, ?> m = (Map<?, ?>)o;
            return m.size() != 1 ? false : m.entrySet().iterator().next().equals(this.entrySet().iterator().next());
         }
      }

      @Override
      public String toString() {
         return "{" + this.key + "=>" + this.value + "}";
      }
   }
}

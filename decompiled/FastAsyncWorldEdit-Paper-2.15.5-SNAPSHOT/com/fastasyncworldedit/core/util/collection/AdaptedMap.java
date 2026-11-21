package com.fastasyncworldedit.core.util.collection;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AdaptedMap<K, V, K2, V2> implements IAdaptedMap<K, V, K2, V2> {
   private final Map<K2, V2> parent;
   private final Function<V2, V> value2;
   private final Function<K2, K> key2;
   private final Function<V, V2> value;
   private final Function<K, K2> key;
   private static final Function SAME = o -> o;
   private static final Function IMMUTABLE = o -> {
      throw new UnsupportedOperationException("Immutable");
   };

   public static <K, K2, V> AdaptedMap<K, V, K2, V> keys(Map<K2, V> parent, Function<K, K2> key, Function<K2, K> key2) {
      return new AdaptedMap<>(parent, key, key2, SAME, SAME);
   }

   public static <K, V, V2> AdaptedMap<K, V, K, V2> values(Map<K, V2> parent, Function<V, V2> value, Function<V2, V> value2) {
      return new AdaptedMap<>(parent, SAME, SAME, value, value2);
   }

   public static <K, K2, V, V2> AdaptedMap<K, V, K2, V2> immutable(Map<K2, V2> parent, Function<K2, K> key, Function<V2, V> value) {
      return new AdaptedMap<>(parent, IMMUTABLE, key, IMMUTABLE, value);
   }

   public AdaptedMap(Map<K2, V2> parent, Function<K, K2> key, Function<K2, K> key2, Function<V, V2> value, Function<V2, V> value2) {
      this.parent = parent;
      this.key = key;
      this.value = value;
      this.key2 = key2;
      this.value2 = value2;
   }

   @Override
   public Map<K2, V2> getParent() {
      return this.parent;
   }

   @Override
   public K2 adaptKey(K key) {
      return this.key.apply(key);
   }

   @Override
   public V2 adaptValue(V value) {
      return this.value.apply(value);
   }

   @Override
   public K adaptKey2(K2 key) {
      return this.key2.apply(key);
   }

   @Override
   public V adaptValue2(V2 value) {
      return this.value2.apply(value);
   }

   @Nonnull
   @Override
   public Set<Entry<K, V>> entrySet() {
      return (Set<Entry<K, V>>)(this.isEmpty()
         ? Collections.emptySet()
         : new AdaptedSetCollection<>(this.getParent().entrySet(), new com.google.common.base.Function<Entry<K2, V2>, Entry<K, V>>() {
            private final AdaptedMap<K, V, K2, V2>.AdaptedPair entry = AdaptedMap.this.new AdaptedPair();

            public Entry<K, V> apply(@Nullable Entry<K2, V2> input) {
               this.entry.input = input;
               return this.entry;
            }
         }));
   }

   public class AdaptedPair implements Entry<K, V> {
      private Entry<K2, V2> input;

      @Override
      public K getKey() {
         return AdaptedMap.this.adaptKey2(this.input.getKey());
      }

      @Override
      public V getValue() {
         return AdaptedMap.this.adaptValue2(this.input.getValue());
      }

      @Override
      public V setValue(V value) {
         return AdaptedMap.this.adaptValue2(this.input.setValue(AdaptedMap.this.adaptValue(value)));
      }

      @Override
      public boolean equals(Object o) {
         return !(o instanceof Entry) ? false : Objects.equals(((Entry)o).getKey(), this.getKey()) && Objects.equals(((Entry)o).getValue(), this.getValue());
      }

      @Override
      public int hashCode() {
         return 1337;
      }
   }
}

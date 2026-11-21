package com.fastasyncworldedit.core.util.collection;

import com.google.common.base.Function;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IAdaptedMap<K, V, K2, V2> extends Map<K, V> {
   Map<K2, V2> getParent();

   K2 adaptKey(K key);

   V2 adaptValue(V value);

   K adaptKey2(K2 key);

   V adaptValue2(V2 value);

   @Override
   default int size() {
      return this.getParent().size();
   }

   @Override
   default boolean isEmpty() {
      return this.getParent().isEmpty();
   }

   @Override
   default boolean containsKey(Object key) {
      return this.getParent().containsKey(this.adaptKey((K)key));
   }

   @Override
   default boolean containsValue(Object value) {
      return this.getParent().containsValue(this.adaptValue((V)value));
   }

   @Override
   default V get(Object key) {
      return this.adaptValue2(this.getParent().get(this.adaptKey((K)key)));
   }

   @Nullable
   @Override
   default V put(K key, V value) {
      return this.adaptValue2(this.getParent().put(this.adaptKey(key), this.adaptValue(value)));
   }

   @Override
   default V remove(Object key) {
      return this.adaptValue2(this.getParent().remove(this.adaptKey((K)key)));
   }

   @Override
   default void putAll(@Nonnull Map<? extends K, ? extends V> m) {
      for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
         this.put((K)entry.getKey(), (V)entry.getValue());
      }
   }

   @Override
   default void clear() {
      this.getParent().clear();
   }

   @Nonnull
   @Override
   default Set<K> keySet() {
      return (Set<K>)(this.isEmpty() ? Collections.emptySet() : new AdaptedSetCollection<>(this.getParent().keySet(), this::adaptKey2));
   }

   @Nonnull
   @Override
   default Collection<V> values() {
      return (Collection<V>)(this.isEmpty() ? Collections.emptySet() : new AdaptedSetCollection<>(this.getParent().values(), this::adaptValue2));
   }

   @Nonnull
   @Override
   default Set<Entry<K, V>> entrySet() {
      return (Set<Entry<K, V>>)(this.isEmpty()
         ? Collections.emptySet()
         : new AdaptedSetCollection<>(this.getParent().entrySet(), new Function<Entry<K2, V2>, Entry<K, V>>() {
            private final MutablePair<K, V> entry = new MutablePair<>();

            public Entry<K, V> apply(@Nullable Entry<K2, V2> input) {
               this.entry.setKey((K)IAdaptedMap.this.adaptKey2(input.getKey()));
               this.entry.setValue((V)IAdaptedMap.this.adaptValue2(input.getValue()));
               return this.entry;
            }
         }));
   }
}

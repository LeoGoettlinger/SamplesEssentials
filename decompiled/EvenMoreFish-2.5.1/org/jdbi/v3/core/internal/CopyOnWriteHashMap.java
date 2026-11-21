package org.jdbi.v3.core.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public final class CopyOnWriteHashMap<K, V> implements ConcurrentMap<K, V> {
   private static final AtomicReferenceFieldUpdater<CopyOnWriteHashMap, Map> U = AtomicReferenceFieldUpdater.newUpdater(
      CopyOnWriteHashMap.class, Map.class, "m"
   );
   private volatile Map<K, V> m;

   public CopyOnWriteHashMap() {
      this.clear();
   }

   public CopyOnWriteHashMap(Map<K, V> that) {
      if (that.getClass() == CopyOnWriteHashMap.class) {
         this.set(((CopyOnWriteHashMap)that).get());
      } else {
         this.set(new HashMap<>(that));
      }
   }

   private AtomicReferenceFieldUpdater<CopyOnWriteHashMap<K, V>, Map<K, V>> u() {
      return U;
   }

   private Map<K, V> get() {
      return this.u().get(this);
   }

   private void set(Map<K, V> m) {
      this.u().set(this, m);
   }

   private Map<K, V> getAndUpdate(UnaryOperator<Map<K, V>> updater) {
      return this.u().getAndUpdate(this, updater);
   }

   private Map<K, V> updateAndGet(UnaryOperator<Map<K, V>> updater) {
      return this.u().updateAndGet(this, updater);
   }

   @Override
   public int size() {
      return this.get().size();
   }

   @Override
   public boolean isEmpty() {
      return this.get().isEmpty();
   }

   @Override
   public boolean containsKey(Object key) {
      return this.get().containsKey(key);
   }

   @Override
   public boolean containsValue(Object value) {
      return this.get().containsValue(value);
   }

   @Override
   public V get(Object key) {
      return this.get().get(key);
   }

   @Override
   public V put(K key, V value) {
      return this.getAndUpdate(old -> {
         Map<K, V> updated = new HashMap<>(old);
         updated.put(key, value);
         return updated;
      }).get(key);
   }

   @Override
   public V putIfAbsent(K key, V value) {
      return this.getAndUpdate(old -> {
         if (old.get(key) != null) {
            return old;
         } else {
            Map<K, V> updated = new HashMap<>(old);
            updated.putIfAbsent(key, value);
            return updated;
         }
      }).get(key);
   }

   @Override
   public void putAll(Map<? extends K, ? extends V> that) {
      this.updateAndGet(old -> {
         Map<K, V> updated = new HashMap<>(old);
         updated.putAll(that);
         return updated;
      });
   }

   @Override
   public V remove(Object key) {
      return this.getAndUpdate(old -> {
         if (!old.containsKey(key)) {
            return old;
         } else {
            Map<K, V> updated = new HashMap<>(old);
            updated.remove(key);
            return updated;
         }
      }).get(key);
   }

   @Override
   public boolean remove(Object key, Object value) {
      return Objects.equals(this.getAndUpdate(old -> {
         if (!Objects.equals(old.get(key), value)) {
            return old;
         } else {
            Map<K, V> updated = new HashMap<>(old);
            updated.remove(key, value);
            return updated;
         }
      }).get(key), value);
   }

   @Override
   public V replace(K key, V value) {
      return this.getAndUpdate(old -> {
         if (!old.containsKey(key)) {
            return old;
         } else {
            Map<K, V> updated = new HashMap<>(old);
            updated.replace(key, value);
            return updated;
         }
      }).get(key);
   }

   @Override
   public boolean replace(K key, V oldValue, V newValue) {
      return Objects.equals(this.getAndUpdate(old -> {
         if (!Objects.equals(old.get(key), oldValue)) {
            return old;
         } else {
            Map<K, V> updated = new HashMap<>(old);
            updated.put(key, newValue);
            return updated;
         }
      }).get(key), oldValue);
   }

   @Override
   public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return this.updateAndGet(old -> {
         Map<K, V> updated = new HashMap<>(old);
         updated.compute(key, remappingFunction);
         return updated;
      }).get(key);
   }

   @Override
   public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
      return this.updateAndGet(old -> {
         if (old.containsKey(key)) {
            return old;
         } else {
            Map<K, V> updated = new HashMap<>(old);
            updated.computeIfAbsent(key, mappingFunction);
            return updated;
         }
      }).get(key);
   }

   @Override
   public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
      return this.updateAndGet(old -> {
         if (!old.containsKey(key)) {
            return old;
         } else {
            Map<K, V> updated = new HashMap<>(old);
            updated.computeIfPresent(key, remappingFunction);
            return updated;
         }
      }).get(key);
   }

   @Override
   public V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
      return this.updateAndGet(old -> {
         Map<K, V> updated = new HashMap<>(old);
         updated.merge(key, value, remappingFunction);
         return updated;
      }).get(key);
   }

   @Override
   public void clear() {
      this.set(new HashMap<>());
   }

   @Override
   public boolean equals(Object obj) {
      return this.get().equals(obj);
   }

   @Override
   public int hashCode() {
      return this.get().hashCode();
   }

   @Override
   public V getOrDefault(Object key, V defaultValue) {
      return this.get().getOrDefault(key, defaultValue);
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      this.get().forEach(action);
   }

   @Override
   public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
      this.updateAndGet(old -> {
         Map<K, V> updated = new HashMap<>(old);
         updated.replaceAll(function);
         return updated;
      });
   }

   @Override
   public Set<K> keySet() {
      return Collections.unmodifiableSet(this.get().keySet());
   }

   @Override
   public Collection<V> values() {
      return Collections.unmodifiableCollection(this.get().values());
   }

   @Override
   public Set<Entry<K, V>> entrySet() {
      return Collections.unmodifiableMap(this.get()).entrySet();
   }
}

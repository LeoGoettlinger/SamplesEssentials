package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotCall;
import com.google.errorprone.annotations.DoNotMock;
import com.google.j2objc.annotations.Weak;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible(
   emulated = true
)
public abstract class ImmutableMultimap<K, V> extends BaseImmutableMultimap<K, V> implements Serializable {
   final transient ImmutableMap<K, ? extends ImmutableCollection<V>> map;
   final transient int size;
   @J2ktIncompatible
   private static final long serialVersionUID = 0L;

   public static <K, V> ImmutableMultimap<K, V> of() {
      return ImmutableListMultimap.of();
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1) {
      return ImmutableListMultimap.of(k1, v1);
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2) {
      return ImmutableListMultimap.of(k1, v1, k2, v2);
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
      return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3);
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
      return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4);
   }

   public static <K, V> ImmutableMultimap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
      return ImmutableListMultimap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
   }

   public static <K, V> ImmutableMultimap.Builder<K, V> builder() {
      return new ImmutableMultimap.Builder<>();
   }

   public static <K, V> ImmutableMultimap<K, V> copyOf(Multimap<? extends K, ? extends V> multimap) {
      if (multimap instanceof ImmutableMultimap) {
         ImmutableMultimap<K, V> kvMultimap = (ImmutableMultimap<K, V>)multimap;
         if (!kvMultimap.isPartialView()) {
            return kvMultimap;
         }
      }

      return ImmutableListMultimap.copyOf(multimap);
   }

   public static <K, V> ImmutableMultimap<K, V> copyOf(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
      return ImmutableListMultimap.copyOf(entries);
   }

   ImmutableMultimap(ImmutableMap<K, ? extends ImmutableCollection<V>> map, int size) {
      this.map = map;
      this.size = size;
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   public ImmutableCollection<V> removeAll(@CheckForNull Object key) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   public ImmutableCollection<V> replaceValues(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final void clear() {
      throw new UnsupportedOperationException();
   }

   public abstract ImmutableCollection<V> get(K key);

   public abstract ImmutableMultimap<V, K> inverse();

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean put(K key, V value) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean putAll(K key, Iterable<? extends V> values) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean putAll(Multimap<? extends K, ? extends V> multimap) {
      throw new UnsupportedOperationException();
   }

   @Deprecated
   @CanIgnoreReturnValue
   @DoNotCall("Always throws UnsupportedOperationException")
   @Override
   public final boolean remove(@CheckForNull Object key, @CheckForNull Object value) {
      throw new UnsupportedOperationException();
   }

   boolean isPartialView() {
      return this.map.isPartialView();
   }

   @Override
   public boolean containsKey(@CheckForNull Object key) {
      return this.map.containsKey(key);
   }

   @Override
   public boolean containsValue(@CheckForNull Object value) {
      return value != null && super.containsValue(value);
   }

   @Override
   public int size() {
      return this.size;
   }

   public ImmutableSet<K> keySet() {
      return this.map.keySet();
   }

   @Override
   Set<K> createKeySet() {
      throw new AssertionError("unreachable");
   }

   public ImmutableMap<K, Collection<V>> asMap() {
      return (ImmutableMap<K, Collection<V>>)this.map;
   }

   @Override
   Map<K, Collection<V>> createAsMap() {
      throw new AssertionError("should never be called");
   }

   public ImmutableCollection<Entry<K, V>> entries() {
      return (ImmutableCollection<Entry<K, V>>)super.entries();
   }

   ImmutableCollection<Entry<K, V>> createEntries() {
      return new ImmutableMultimap.EntryCollection<>(this);
   }

   UnmodifiableIterator<Entry<K, V>> entryIterator() {
      return new UnmodifiableIterator<Entry<K, V>>() {
         final Iterator<? extends Entry<K, ? extends ImmutableCollection<V>>> asMapItr = ImmutableMultimap.this.map.entrySet().iterator();
         @CheckForNull
         K currentKey = (K)null;
         Iterator<V> valueItr = Iterators.emptyIterator();

         @Override
         public boolean hasNext() {
            return this.valueItr.hasNext() || this.asMapItr.hasNext();
         }

         public Entry<K, V> next() {
            if (!this.valueItr.hasNext()) {
               Entry<K, ? extends ImmutableCollection<V>> entry = (Entry<K, ? extends ImmutableCollection<V>>)this.asMapItr.next();
               this.currentKey = entry.getKey();
               this.valueItr = entry.getValue().iterator();
            }

            return Maps.immutableEntry(Objects.requireNonNull(this.currentKey), this.valueItr.next());
         }
      };
   }

   @Override
   Spliterator<Entry<K, V>> entrySpliterator() {
      return CollectSpliterators.flatMap(this.asMap().entrySet().spliterator(), keyToValueCollectionEntry -> {
         K key = keyToValueCollectionEntry.getKey();
         Collection<V> valueCollection = keyToValueCollectionEntry.getValue();
         return CollectSpliterators.map(valueCollection.spliterator(), value -> Maps.immutableEntry(key, (V)value));
      }, 64 | (this instanceof SetMultimap ? 1 : 0), this.size());
   }

   @Override
   public void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);
      this.asMap().forEach((key, valueCollection) -> valueCollection.forEach(value -> action.accept(key, value)));
   }

   public ImmutableMultiset<K> keys() {
      return (ImmutableMultiset<K>)super.keys();
   }

   ImmutableMultiset<K> createKeys() {
      return new ImmutableMultimap.Keys();
   }

   public ImmutableCollection<V> values() {
      return (ImmutableCollection<V>)super.values();
   }

   ImmutableCollection<V> createValues() {
      return new ImmutableMultimap.Values<>(this);
   }

   UnmodifiableIterator<V> valueIterator() {
      return new UnmodifiableIterator<V>() {
         Iterator<? extends ImmutableCollection<V>> valueCollectionItr = ImmutableMultimap.this.map.values().iterator();
         Iterator<V> valueItr = Iterators.emptyIterator();

         @Override
         public boolean hasNext() {
            return this.valueItr.hasNext() || this.valueCollectionItr.hasNext();
         }

         @Override
         public V next() {
            if (!this.valueItr.hasNext()) {
               this.valueItr = this.valueCollectionItr.next().iterator();
            }

            return this.valueItr.next();
         }
      };
   }

   @DoNotMock
   public static class Builder<K, V> {
      final Map<K, Collection<V>> builderMap = Platform.preservesInsertionOrderOnPutsMap();
      @CheckForNull
      Comparator<? super K> keyComparator;
      @CheckForNull
      Comparator<? super V> valueComparator;

      Collection<V> newMutableValueCollection() {
         return new ArrayList<>();
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> put(K key, V value) {
         CollectPreconditions.checkEntryNotNull(key, value);
         Collection<V> valueCollection = this.builderMap.get(key);
         if (valueCollection == null) {
            this.builderMap.put(key, valueCollection = this.newMutableValueCollection());
         }

         valueCollection.add(value);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> put(Entry<? extends K, ? extends V> entry) {
         return this.put((K)entry.getKey(), (V)entry.getValue());
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> putAll(Iterable<? extends Entry<? extends K, ? extends V>> entries) {
         for (Entry<? extends K, ? extends V> entry : entries) {
            this.put(entry);
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> putAll(K key, Iterable<? extends V> values) {
         if (key == null) {
            throw new NullPointerException("null key in entry: null=" + Iterables.toString(values));
         } else {
            Collection<V> valueCollection = this.builderMap.get(key);
            if (valueCollection != null) {
               for (V value : values) {
                  CollectPreconditions.checkEntryNotNull(key, value);
                  valueCollection.add(value);
               }

               return this;
            } else {
               Iterator<? extends V> valuesItr = values.iterator();
               if (!valuesItr.hasNext()) {
                  return this;
               } else {
                  valueCollection = this.newMutableValueCollection();

                  while (valuesItr.hasNext()) {
                     V value = (V)valuesItr.next();
                     CollectPreconditions.checkEntryNotNull(key, value);
                     valueCollection.add(value);
                  }

                  this.builderMap.put(key, valueCollection);
                  return this;
               }
            }
         }
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> putAll(K key, V... values) {
         return this.putAll(key, Arrays.asList(values));
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> putAll(Multimap<? extends K, ? extends V> multimap) {
         for (Entry<? extends K, ? extends Collection<? extends V>> entry : multimap.asMap().entrySet()) {
            this.putAll((K)entry.getKey(), (Iterable<? extends V>)entry.getValue());
         }

         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> orderKeysBy(Comparator<? super K> keyComparator) {
         this.keyComparator = Preconditions.checkNotNull(keyComparator);
         return this;
      }

      @CanIgnoreReturnValue
      public ImmutableMultimap.Builder<K, V> orderValuesBy(Comparator<? super V> valueComparator) {
         this.valueComparator = Preconditions.checkNotNull(valueComparator);
         return this;
      }

      @CanIgnoreReturnValue
      ImmutableMultimap.Builder<K, V> combine(ImmutableMultimap.Builder<K, V> other) {
         for (Entry<K, Collection<V>> entry : other.builderMap.entrySet()) {
            this.putAll(entry.getKey(), entry.getValue());
         }

         return this;
      }

      public ImmutableMultimap<K, V> build() {
         Collection<Entry<K, Collection<V>>> mapEntries = this.builderMap.entrySet();
         if (this.keyComparator != null) {
            mapEntries = Ordering.from(this.keyComparator).onKeys().immutableSortedCopy(mapEntries);
         }

         return ImmutableListMultimap.fromMapEntries(mapEntries, this.valueComparator);
      }
   }

   private static class EntryCollection<K, V> extends ImmutableCollection<Entry<K, V>> {
      @Weak
      final ImmutableMultimap<K, V> multimap;
      private static final long serialVersionUID = 0L;

      EntryCollection(ImmutableMultimap<K, V> multimap) {
         this.multimap = multimap;
      }

      @Override
      public UnmodifiableIterator<Entry<K, V>> iterator() {
         return this.multimap.entryIterator();
      }

      @Override
      boolean isPartialView() {
         return this.multimap.isPartialView();
      }

      @Override
      public int size() {
         return this.multimap.size();
      }

      @Override
      public boolean contains(@CheckForNull Object object) {
         if (object instanceof Entry) {
            Entry<?, ?> entry = (Entry<?, ?>)object;
            return this.multimap.containsEntry(entry.getKey(), entry.getValue());
         } else {
            return false;
         }
      }
   }

   @GwtIncompatible
   @J2ktIncompatible
   static class FieldSettersHolder {
      static final Serialization.FieldSetter<ImmutableMultimap> MAP_FIELD_SETTER = Serialization.getFieldSetter(ImmutableMultimap.class, "map");
      static final Serialization.FieldSetter<ImmutableMultimap> SIZE_FIELD_SETTER = Serialization.getFieldSetter(ImmutableMultimap.class, "size");
   }

   class Keys extends ImmutableMultiset<K> {
      @Override
      public boolean contains(@CheckForNull Object object) {
         return ImmutableMultimap.this.containsKey(object);
      }

      @Override
      public int count(@CheckForNull Object element) {
         Collection<V> values = (Collection<V>)ImmutableMultimap.this.map.get(element);
         return values == null ? 0 : values.size();
      }

      @Override
      public ImmutableSet<K> elementSet() {
         return ImmutableMultimap.this.keySet();
      }

      @Override
      public int size() {
         return ImmutableMultimap.this.size();
      }

      @Override
      Multiset.Entry<K> getEntry(int index) {
         Map.Entry<K, ? extends Collection<V>> entry = ImmutableMultimap.this.map.entrySet().asList().get(index);
         return Multisets.immutableEntry(entry.getKey(), entry.getValue().size());
      }

      @Override
      boolean isPartialView() {
         return true;
      }

      @GwtIncompatible
      @J2ktIncompatible
      @Override
      Object writeReplace() {
         return new ImmutableMultimap.KeysSerializedForm(ImmutableMultimap.this);
      }

      @GwtIncompatible
      @J2ktIncompatible
      private void readObject(ObjectInputStream stream) throws InvalidObjectException {
         throw new InvalidObjectException("Use KeysSerializedForm");
      }
   }

   @GwtIncompatible
   @J2ktIncompatible
   private static final class KeysSerializedForm implements Serializable {
      final ImmutableMultimap<?, ?> multimap;

      KeysSerializedForm(ImmutableMultimap<?, ?> multimap) {
         this.multimap = multimap;
      }

      Object readResolve() {
         return this.multimap.keys();
      }
   }

   private static final class Values<K, V> extends ImmutableCollection<V> {
      @Weak
      private final transient ImmutableMultimap<K, V> multimap;
      @J2ktIncompatible
      private static final long serialVersionUID = 0L;

      Values(ImmutableMultimap<K, V> multimap) {
         this.multimap = multimap;
      }

      @Override
      public boolean contains(@CheckForNull Object object) {
         return this.multimap.containsValue(object);
      }

      @Override
      public UnmodifiableIterator<V> iterator() {
         return this.multimap.valueIterator();
      }

      @GwtIncompatible
      @Override
      int copyIntoArray(Object[] dst, int offset) {
         for (ImmutableCollection<V> valueCollection : this.multimap.map.values()) {
            offset = valueCollection.copyIntoArray(dst, offset);
         }

         return offset;
      }

      @Override
      public int size() {
         return this.multimap.size();
      }

      @Override
      boolean isPartialView() {
         return true;
      }
   }
}

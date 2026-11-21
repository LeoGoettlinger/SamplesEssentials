package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CompatibleWith;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import javax.annotation.CheckForNull;

@DoNotMock("Use ImmutableMultimap, HashMultimap, or another implementation")
@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface Multimap<K, V> {
   int size();

   boolean isEmpty();

   boolean containsKey(@CheckForNull @CompatibleWith("K") Object key);

   boolean containsValue(@CheckForNull @CompatibleWith("V") Object value);

   boolean containsEntry(@CheckForNull @CompatibleWith("K") Object key, @CheckForNull @CompatibleWith("V") Object value);

   @CanIgnoreReturnValue
   boolean put(@ParametricNullness K key, @ParametricNullness V value);

   @CanIgnoreReturnValue
   boolean remove(@CheckForNull @CompatibleWith("K") Object key, @CheckForNull @CompatibleWith("V") Object value);

   @CanIgnoreReturnValue
   boolean putAll(@ParametricNullness K key, Iterable<? extends V> values);

   @CanIgnoreReturnValue
   boolean putAll(Multimap<? extends K, ? extends V> multimap);

   @CanIgnoreReturnValue
   Collection<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values);

   @CanIgnoreReturnValue
   Collection<V> removeAll(@CheckForNull @CompatibleWith("K") Object key);

   void clear();

   Collection<V> get(@ParametricNullness K key);

   Set<K> keySet();

   Multiset<K> keys();

   Collection<V> values();

   Collection<Entry<K, V>> entries();

   default void forEach(BiConsumer<? super K, ? super V> action) {
      Preconditions.checkNotNull(action);
      this.entries().forEach(entry -> action.accept(entry.getKey(), entry.getValue()));
   }

   Map<K, Collection<V>> asMap();

   @Override
   boolean equals(@CheckForNull Object obj);

   @Override
   int hashCode();
}

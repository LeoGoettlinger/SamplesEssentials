package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface SetMultimap<K, V> extends Multimap<K, V> {
   Set<V> get(@ParametricNullness K key);

   @CanIgnoreReturnValue
   Set<V> removeAll(@CheckForNull Object key);

   @CanIgnoreReturnValue
   Set<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values);

   Set<Entry<K, V>> entries();

   @Override
   Map<K, Collection<V>> asMap();

   @Override
   boolean equals(@CheckForNull Object obj);
}

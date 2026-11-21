package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface SortedSetMultimap<K, V> extends SetMultimap<K, V> {
   SortedSet<V> get(@ParametricNullness K key);

   @CanIgnoreReturnValue
   SortedSet<V> removeAll(@CheckForNull Object key);

   @CanIgnoreReturnValue
   SortedSet<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values);

   @Override
   Map<K, Collection<V>> asMap();

   @CheckForNull
   Comparator<? super V> valueComparator();
}

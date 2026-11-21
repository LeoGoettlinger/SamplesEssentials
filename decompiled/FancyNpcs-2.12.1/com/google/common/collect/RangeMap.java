package com.google.common.collect;

import com.google.common.annotations.GwtIncompatible;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import javax.annotation.CheckForNull;

@DoNotMock("Use ImmutableRangeMap or TreeRangeMap")
@ElementTypesAreNonnullByDefault
@GwtIncompatible
public interface RangeMap<K extends Comparable, V> {
   @CheckForNull
   V get(K key);

   @CheckForNull
   Entry<Range<K>, V> getEntry(K key);

   Range<K> span();

   void put(Range<K> range, V value);

   void putCoalescing(Range<K> range, V value);

   void putAll(RangeMap<K, ? extends V> rangeMap);

   void clear();

   void remove(Range<K> range);

   void merge(Range<K> range, @CheckForNull V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction);

   Map<Range<K>, V> asMapOfRanges();

   Map<Range<K>, V> asDescendingMapOfRanges();

   RangeMap<K, V> subRangeMap(Range<K> range);

   @Override
   boolean equals(@CheckForNull Object o);

   @Override
   int hashCode();

   @Override
   String toString();
}

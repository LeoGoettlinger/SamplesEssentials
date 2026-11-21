package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface ListMultimap<K, V> extends Multimap<K, V> {
   List<V> get(@ParametricNullness K key);

   @CanIgnoreReturnValue
   List<V> removeAll(@CheckForNull Object key);

   @CanIgnoreReturnValue
   List<V> replaceValues(@ParametricNullness K key, Iterable<? extends V> values);

   @Override
   Map<K, Collection<V>> asMap();

   @Override
   boolean equals(@CheckForNull Object obj);
}

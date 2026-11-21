package com.google.common.cache;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface LoadingCache<K, V> extends Cache<K, V>, Function<K, V> {
   @CanIgnoreReturnValue
   V get(K key) throws ExecutionException;

   @CanIgnoreReturnValue
   V getUnchecked(K key);

   @CanIgnoreReturnValue
   ImmutableMap<K, V> getAll(Iterable<? extends K> keys) throws ExecutionException;

   @Deprecated
   @Override
   V apply(K key);

   void refresh(K key);

   @Override
   ConcurrentMap<K, V> asMap();
}

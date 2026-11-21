package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public abstract class ForwardingConcurrentMap<K, V> extends ForwardingMap<K, V> implements ConcurrentMap<K, V> {
   protected ForwardingConcurrentMap() {
   }

   protected abstract ConcurrentMap<K, V> delegate();

   @CheckForNull
   @CanIgnoreReturnValue
   @Override
   public V putIfAbsent(K key, V value) {
      return this.delegate().putIfAbsent(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean remove(@CheckForNull Object key, @CheckForNull Object value) {
      return this.delegate().remove(key, value);
   }

   @CheckForNull
   @CanIgnoreReturnValue
   @Override
   public V replace(K key, V value) {
      return this.delegate().replace(key, value);
   }

   @CanIgnoreReturnValue
   @Override
   public boolean replace(K key, V oldValue, V newValue) {
      return this.delegate().replace(key, oldValue, newValue);
   }
}

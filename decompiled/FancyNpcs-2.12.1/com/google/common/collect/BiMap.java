package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map;
import java.util.Set;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public interface BiMap<K, V> extends Map<K, V> {
   @CheckForNull
   @CanIgnoreReturnValue
   @Override
   V put(@ParametricNullness K key, @ParametricNullness V value);

   @CheckForNull
   @CanIgnoreReturnValue
   V forcePut(@ParametricNullness K key, @ParametricNullness V value);

   @Override
   void putAll(Map<? extends K, ? extends V> map);

   Set<V> values();

   BiMap<V, K> inverse();
}

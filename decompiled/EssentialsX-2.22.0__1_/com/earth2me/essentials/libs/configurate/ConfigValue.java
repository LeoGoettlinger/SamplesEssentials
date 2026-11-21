package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;

interface ConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> {
   @Nullable
   Object get();

   void set(@Nullable Object value);

   @Nullable
   A putChild(Object key, @Nullable A value);

   @Nullable
   A putChildIfAbsent(Object key, @Nullable A value);

   @Nullable
   A child(@Nullable Object key);

   Iterable<A> iterateChildren();

   ConfigValue<N, A> copy(A holder);

   boolean isEmpty();

   void clear();
}

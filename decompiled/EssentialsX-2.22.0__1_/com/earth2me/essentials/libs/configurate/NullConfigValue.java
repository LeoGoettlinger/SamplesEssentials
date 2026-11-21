package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.util.Collections;

final class NullConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> implements ConfigValue<N, A> {
   private static final NullConfigValue INSTANCE = new NullConfigValue();

   static <N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> NullConfigValue<N, A> instance() {
      return INSTANCE;
   }

   private NullConfigValue() {
   }

   @Nullable
   @Override
   public Object get() {
      return null;
   }

   @Override
   public void set(@Nullable final Object value) {
      throw new UnsupportedOperationException("Value should be changed from null type before setting value");
   }

   @Nullable
   @Override
   public A putChild(final Object key, @Nullable final A value) {
      return null;
   }

   @Nullable
   @Override
   public A putChildIfAbsent(final Object key, @Nullable final A value) {
      return null;
   }

   @Nullable
   @Override
   public A child(@Nullable final Object key) {
      return null;
   }

   @Override
   public Iterable<A> iterateChildren() {
      return Collections.emptySet();
   }

   public NullConfigValue<N, A> copy(final A holder) {
      return instance();
   }

   @Override
   public boolean isEmpty() {
      return true;
   }

   @Override
   public void clear() {
   }

   @Override
   public boolean equals(final Object o) {
      return o instanceof NullConfigValue;
   }

   @Override
   public int hashCode() {
      return 1009;
   }

   @Override
   public String toString() {
      return "NullConfigValue{}";
   }
}

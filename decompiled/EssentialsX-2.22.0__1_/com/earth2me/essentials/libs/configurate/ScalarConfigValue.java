package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

final class ScalarConfigValue<N extends ScopedConfigurationNode<N>, A extends AbstractConfigurationNode<N, A>> implements ConfigValue<N, A> {
   private final A holder;
   @Nullable
   private volatile Object value;

   ScalarConfigValue(final A holder) {
      this.holder = holder;
   }

   @Nullable
   @Override
   public Object get() {
      return this.value;
   }

   @Override
   public void set(@Nullable final Object value) {
      if (value != null && !this.holder.options().acceptsType(value.getClass())) {
         throw new IllegalArgumentException("Configuration does not accept objects of type " + value.getClass());
      } else {
         this.value = value;
      }
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

   public ScalarConfigValue<N, A> copy(final A holder) {
      ScalarConfigValue<N, A> copy = new ScalarConfigValue<>(holder);
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean isEmpty() {
      Object value = this.value;
      return value instanceof String && ((String)value).isEmpty() || value instanceof Collection && ((Collection)value).isEmpty();
   }

   @Override
   public void clear() {
      this.value = null;
   }

   @Override
   public boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof ScalarConfigValue)) {
         return false;
      } else {
         ScalarConfigValue<?, ?> that = (ScalarConfigValue<?, ?>)other;
         return Objects.equals(this.value, that.value);
      }
   }

   @Override
   public int hashCode() {
      return 7 + Objects.hashCode(this.value);
   }

   @Override
   public String toString() {
      return "ScalarConfigValue{value=" + this.value + '}';
   }
}

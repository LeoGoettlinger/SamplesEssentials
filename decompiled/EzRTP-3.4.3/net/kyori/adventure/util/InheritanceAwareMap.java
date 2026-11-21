package net.kyori.adventure.util;

import net.kyori.adventure.builder.AbstractBuilder;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;

public interface InheritanceAwareMap<C, V> {
   static <K, E> InheritanceAwareMap<K, E> empty() {
      return InheritanceAwareMapImpl.EMPTY;
   }

   static <K, E> InheritanceAwareMap.Builder<K, E> builder() {
      return new InheritanceAwareMapImpl.BuilderImpl();
   }

   static <K, E> InheritanceAwareMap.Builder<K, E> builder(final InheritanceAwareMap<? extends K, ? extends E> existing) {
      return new InheritanceAwareMapImpl.BuilderImpl().putAll(existing);
   }

   boolean containsKey(final Class<? extends C> clazz);

   @Nullable
   V get(final Class<? extends C> clazz);

   @CheckReturnValue
   InheritanceAwareMap<C, V> with(final Class<? extends C> clazz, final V value);

   @CheckReturnValue
   InheritanceAwareMap<C, V> without(final Class<? extends C> clazz);

   public interface Builder<C, V> extends AbstractBuilder<InheritanceAwareMap<C, V>> {
      InheritanceAwareMap.Builder<C, V> strict(final boolean strict);

      InheritanceAwareMap.Builder<C, V> put(final Class<? extends C> clazz, final V value);

      InheritanceAwareMap.Builder<C, V> remove(final Class<? extends C> clazz);

      InheritanceAwareMap.Builder<C, V> putAll(final InheritanceAwareMap<? extends C, ? extends V> map);
   }
}

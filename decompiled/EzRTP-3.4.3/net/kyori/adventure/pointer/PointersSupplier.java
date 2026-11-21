package net.kyori.adventure.pointer;

import java.util.function.Function;
import net.kyori.adventure.builder.AbstractBuilder;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public sealed interface PointersSupplier<T> permits PointersSupplierImpl {
   static <T> PointersSupplier.Builder<T> builder() {
      return new PointersSupplierImpl.BuilderImpl<>();
   }

   Pointers view(final T instance);

   <P> boolean supports(final Pointer<P> pointer);

   @Nullable
   <P> Function<? super T, P> resolver(final Pointer<P> pointer);

   public sealed interface Builder<T> extends AbstractBuilder<PointersSupplier<T>> permits PointersSupplierImpl.BuilderImpl {
      @Contract("_ -> this")
      PointersSupplier.Builder<T> parent(@Nullable final PointersSupplier<? super T> parent);

      @Contract("_, _ -> this")
      <P> PointersSupplier.Builder<T> resolving(final Pointer<P> pointer, final Function<T, P> resolver);
   }
}

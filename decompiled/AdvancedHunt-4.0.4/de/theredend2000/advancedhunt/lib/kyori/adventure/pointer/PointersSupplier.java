package de.theredend2000.advancedhunt.lib.kyori.adventure.pointer;

import de.theredend2000.advancedhunt.lib.kyori.adventure.builder.AbstractBuilder;
import java.util.function.Function;
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

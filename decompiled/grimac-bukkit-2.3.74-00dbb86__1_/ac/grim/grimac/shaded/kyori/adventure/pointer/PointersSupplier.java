package ac.grim.grimac.shaded.kyori.adventure.pointer;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Contract;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.builder.AbstractBuilder;
import java.util.function.Function;

@ApiStatus.NonExtendable
public interface PointersSupplier<T> {
   @NotNull
   static <T> PointersSupplier.Builder<T> builder() {
      return new PointersSupplierImpl.BuilderImpl<>();
   }

   @NotNull
   Pointers view(@NotNull final T instance);

   <P> boolean supports(@NotNull final Pointer<P> pointer);

   @Nullable
   <P> Function<? super T, P> resolver(@NotNull final Pointer<P> pointer);

   @ApiStatus.NonExtendable
   public interface Builder<T> extends AbstractBuilder<PointersSupplier<T>> {
      @Contract("_ -> this")
      @NotNull
      PointersSupplier.Builder<T> parent(@Nullable final PointersSupplier<? super T> parent);

      @Contract("_, _ -> this")
      @NotNull
      <P> PointersSupplier.Builder<T> resolving(@NotNull final Pointer<P> pointer, @NotNull final Function<T, P> resolver);
   }
}

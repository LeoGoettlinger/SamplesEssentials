package net.kyori.adventure.pointer;

import java.util.Optional;
import java.util.function.Supplier;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public sealed interface Pointers extends Buildable<Pointers.Builder> permits PointersImpl, PointersSupplierImpl.ForwardingPointers {
   @Contract(
      pure = true
   )
   static Pointers empty() {
      return PointersImpl.EMPTY;
   }

   @Contract(
      pure = true
   )
   static Pointers.Builder builder() {
      return new PointersImpl.BuilderImpl();
   }

   <T> Optional<T> get(final Pointer<T> pointer);

   @Contract("_, null -> _; _, !null -> !null")
   @Nullable
   default <T> T getOrDefault(final Pointer<T> pointer, @Nullable final T defaultValue) {
      return this.get(pointer).orElse(defaultValue);
   }

   @UnknownNullability
   default <T> T getOrDefaultFrom(final Pointer<T> pointer, final Supplier<? extends T> defaultValue) {
      return this.get(pointer).orElseGet(defaultValue);
   }

   <T> boolean supports(final Pointer<T> pointer);

   public sealed interface Builder extends AbstractBuilder<Pointers> permits PointersImpl.BuilderImpl {
      @Contract("_, _ -> this")
      default <T> Pointers.Builder withStatic(final Pointer<T> pointer, @Nullable final T value) {
         return this.withDynamic(pointer, () -> value);
      }

      @Contract("_, _ -> this")
      <T> Pointers.Builder withDynamic(final Pointer<T> pointer, Supplier<T> value);
   }
}

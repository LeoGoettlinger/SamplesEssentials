package net.kyori.adventure.pointer;

import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public interface Pointered {
   default <T> Optional<T> get(final Pointer<T> pointer) {
      return this.pointers().get(pointer);
   }

   @Contract("_, null -> _; _, !null -> !null")
   @Nullable
   default <T> T getOrDefault(final Pointer<T> pointer, @Nullable final T defaultValue) {
      return this.pointers().getOrDefault(pointer, defaultValue);
   }

   @UnknownNullability
   default <T> T getOrDefaultFrom(final Pointer<T> pointer, final Supplier<? extends T> defaultValue) {
      return this.pointers().getOrDefaultFrom(pointer, defaultValue);
   }

   default Pointers pointers() {
      return Pointers.empty();
   }
}

package ac.grim.grimac.shaded.kyori.adventure.key;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Objects;

public interface KeyedValue<T> extends Keyed {
   @NotNull
   static <T> KeyedValue<T> keyedValue(@NotNull final Key key, @NotNull final T value) {
      return new KeyedValueImpl<>(key, Objects.requireNonNull(value, "value"));
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static <T> KeyedValue<T> of(@NotNull final Key key, @NotNull final T value) {
      return new KeyedValueImpl<>(key, Objects.requireNonNull(value, "value"));
   }

   @NotNull
   T value();
}

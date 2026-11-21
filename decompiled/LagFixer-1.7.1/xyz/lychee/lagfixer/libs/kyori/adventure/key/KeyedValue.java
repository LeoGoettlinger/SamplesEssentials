package xyz.lychee.lagfixer.libs.kyori.adventure.key;

import java.util.Objects;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

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

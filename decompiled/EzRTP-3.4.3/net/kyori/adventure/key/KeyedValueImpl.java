package net.kyori.adventure.key;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

record KeyedValueImpl<T>(Key key, T value) implements KeyedValue<T> {
   KeyedValueImpl(Key key, T value) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      this.key = key;
      this.value = value;
   }

   @Override
   public boolean equals(@Nullable final Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof KeyedValue<?> that) ? false : this.key.equals(that.key()) && this.value.equals(that.value());
      }
   }
}

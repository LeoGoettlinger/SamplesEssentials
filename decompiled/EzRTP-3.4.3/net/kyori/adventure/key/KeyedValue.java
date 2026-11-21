package net.kyori.adventure.key;

public interface KeyedValue<T> extends Keyed {
   static <T> KeyedValue<T> keyedValue(final Key key, final T value) {
      return new KeyedValueImpl<>(key, value);
   }

   T value();
}

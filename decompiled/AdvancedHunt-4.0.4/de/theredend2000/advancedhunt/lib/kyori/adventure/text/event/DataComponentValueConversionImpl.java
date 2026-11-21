package de.theredend2000.advancedhunt.lib.kyori.adventure.text.event;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import java.util.Objects;
import java.util.function.BiFunction;

record DataComponentValueConversionImpl<I, O>(Class<I> source, Class<O> destination, BiFunction<Key, I, O> conversion)
   implements DataComponentValueConverterRegistry.Conversion<I, O> {
   @Override
   public O convert(final Key key, final I input) {
      return this.conversion.apply(Objects.requireNonNull(key, "key"), Objects.requireNonNull(input, "input"));
   }
}

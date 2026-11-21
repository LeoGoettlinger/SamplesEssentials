package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface ComponentSerializer<I extends Component, O extends Component, R> extends ComponentEncoder<I, R>, ComponentDecoder<R, O> {
   @Override
   O deserialize(final R input);

   @Contract(
      value = "!null -> !null; null -> null",
      pure = true
   )
   @Nullable
   @Override
   default O deserializeOrNull(@Nullable final R input) {
      return ComponentDecoder.super.deserializeOr(input, null);
   }

   @Contract(
      value = "!null, _ -> !null; null, _ -> param2",
      pure = true
   )
   @Nullable
   @Override
   default O deserializeOr(@Nullable final R input, @Nullable final O fallback) {
      return ComponentDecoder.super.deserializeOr(input, fallback);
   }

   @Override
   R serialize(final I component);

   @Contract(
      value = "!null -> !null; null -> null",
      pure = true
   )
   @Nullable
   @Override
   default R serializeOrNull(@Nullable final I component) {
      return this.serializeOr(component, null);
   }

   @Contract(
      value = "!null, _ -> !null; null, _ -> param2",
      pure = true
   )
   @Nullable
   @Override
   default R serializeOr(@Nullable final I component, @Nullable final R fallback) {
      return component == null ? fallback : this.serialize(component);
   }
}

package com.earth2me.essentials.libs.kyori.adventure.text.minimessage.internal.serializer;

import com.earth2me.essentials.libs.kyori.adventure.text.format.Style;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface StyleClaim<V> {
   @NotNull
   static <T> StyleClaim<T> claim(@NotNull final String claimKey, @NotNull final Function<Style, T> lens, @NotNull final BiConsumer<T, TokenEmitter> emitable) {
      return claim(claimKey, lens, $ -> true, emitable);
   }

   @NotNull
   static <T> StyleClaim<T> claim(
      @NotNull final String claimKey,
      @NotNull final Function<Style, T> lens,
      @NotNull final Predicate<T> filter,
      @NotNull final BiConsumer<T, TokenEmitter> emitable
   ) {
      return new StyleClaimImpl(
         Objects.requireNonNull(claimKey, "claimKey"),
         Objects.requireNonNull((Function<Style, V>)lens, "lens"),
         Objects.requireNonNull((Predicate<V>)filter, "filter"),
         Objects.requireNonNull((BiConsumer<V, TokenEmitter>)emitable, "emitable")
      );
   }

   @NotNull
   String claimKey();

   @Nullable
   Emitable apply(@NotNull final Style style);
}

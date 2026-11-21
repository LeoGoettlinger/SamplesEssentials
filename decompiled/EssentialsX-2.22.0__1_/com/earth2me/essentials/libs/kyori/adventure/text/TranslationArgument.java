package com.earth2me.essentials.libs.kyori.adventure.text;

import com.earth2me.essentials.libs.kyori.examination.Examinable;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface TranslationArgument extends TranslationArgumentLike, Examinable {
   @NotNull
   static TranslationArgument bool(final boolean value) {
      return new TranslationArgumentImpl(value);
   }

   @NotNull
   static TranslationArgument numeric(@NotNull final Number value) {
      return new TranslationArgumentImpl(Objects.requireNonNull(value, "value"));
   }

   @NotNull
   static TranslationArgument component(@NotNull final ComponentLike value) {
      return (TranslationArgument)(value instanceof TranslationArgumentLike
         ? ((TranslationArgumentLike)value).asTranslationArgument()
         : new TranslationArgumentImpl(Objects.requireNonNull(Objects.requireNonNull(value, "value").asComponent(), "value.asComponent()")));
   }

   @NotNull
   Object value();

   @NotNull
   @Override
   default TranslationArgument asTranslationArgument() {
      return this;
   }
}

package de.theredend2000.advancedhunt.lib.kyori.adventure.text;

import java.util.Objects;

public sealed interface TranslationArgument extends TranslationArgumentLike permits TranslationArgumentImpl {
   static TranslationArgument bool(final boolean value) {
      return new TranslationArgumentImpl(value);
   }

   static TranslationArgument numeric(final Number value) {
      return new TranslationArgumentImpl(Objects.requireNonNull(value, "value"));
   }

   static TranslationArgument component(final ComponentLike value) {
      return (TranslationArgument)(value instanceof TranslationArgumentLike tal
         ? tal.asTranslationArgument()
         : new TranslationArgumentImpl(Objects.requireNonNull(Objects.requireNonNull(value, "value").asComponent(), "value.asComponent()")));
   }

   Object value();

   @Override
   default TranslationArgument asTranslationArgument() {
      return this;
   }
}

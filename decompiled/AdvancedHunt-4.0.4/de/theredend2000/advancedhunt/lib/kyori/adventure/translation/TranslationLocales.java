package de.theredend2000.advancedhunt.lib.kyori.adventure.translation;

import de.theredend2000.advancedhunt.lib.kyori.adventure.internal.properties.AdventureProperties;
import java.util.Locale;
import java.util.function.Supplier;

final class TranslationLocales {
   private static final Supplier<Locale> GLOBAL;

   private TranslationLocales() {
   }

   static Locale global() {
      return GLOBAL.get();
   }

   static {
      String property = AdventureProperties.DEFAULT_TRANSLATION_LOCALE.value();
      if (property == null || property.isEmpty()) {
         GLOBAL = () -> Locale.US;
      } else if (property.equals("system")) {
         GLOBAL = Locale::getDefault;
      } else {
         Locale locale = Translator.parseLocale(property);
         GLOBAL = () -> locale;
      }
   }
}

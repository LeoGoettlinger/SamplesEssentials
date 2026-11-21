package me.clip.placeholderapi.libs.kyori.adventure.translation;

import java.util.Locale;
import java.util.function.Supplier;
import me.clip.placeholderapi.libs.kyori.adventure.internal.properties.AdventureProperties;

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

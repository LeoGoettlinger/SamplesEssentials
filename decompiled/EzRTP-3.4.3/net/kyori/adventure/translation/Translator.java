package net.kyori.adventure.translation;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.Nullable;

public interface Translator {
   @Nullable
   static Locale parseLocale(final String string) {
      String[] segments = string.split("_", 3);
      int length = segments.length;
      if (length == 1) {
         return Locale.of(string);
      } else if (length == 2) {
         return Locale.of(segments[0], segments[1]);
      } else {
         return length == 3 ? Locale.of(segments[0], segments[1], segments[2]) : null;
      }
   }

   Key name();

   default TriState hasAnyTranslations() {
      return TriState.NOT_SET;
   }

   default boolean canTranslate(final String key, final Locale locale) {
      Component translatedValue = this.translate(Component.translatable(Objects.requireNonNull(key, "key")), Objects.requireNonNull(locale, "locale"));
      return translatedValue != null ? true : this.translate(key, locale) != null;
   }

   @Nullable
   MessageFormat translate(final String key, final Locale locale);

   @Nullable
   default Component translate(final TranslatableComponent component, final Locale locale) {
      return null;
   }
}

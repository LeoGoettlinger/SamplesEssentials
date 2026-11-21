package xyz.lychee.lagfixer.libs.kyori.adventure.translation;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.TranslatableComponent;
import xyz.lychee.lagfixer.libs.kyori.adventure.util.TriState;

public interface Translator {
   @Nullable
   static Locale parseLocale(@NotNull final String string) {
      String[] segments = string.split("_", 3);
      int length = segments.length;
      if (length == 1) {
         return new Locale(string);
      } else if (length == 2) {
         return new Locale(segments[0], segments[1]);
      } else {
         return length == 3 ? new Locale(segments[0], segments[1], segments[2]) : null;
      }
   }

   @NotNull
   Key name();

   @NotNull
   default TriState hasAnyTranslations() {
      return TriState.NOT_SET;
   }

   default boolean canTranslate(@NotNull final String key, @NotNull final Locale locale) {
      Component translatedValue = this.translate(Component.translatable(Objects.requireNonNull(key, "key")), Objects.requireNonNull(locale, "locale"));
      return translatedValue != null ? true : this.translate(key, locale) != null;
   }

   @Nullable
   MessageFormat translate(@NotNull final String key, @NotNull final Locale locale);

   @Nullable
   default Component translate(@NotNull final TranslatableComponent component, @NotNull final Locale locale) {
      return null;
   }
}

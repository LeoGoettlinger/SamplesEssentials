package net.kyori.adventure.translation;

import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

public interface TranslationStore<T> extends Translator {
   static TranslationStore<Component> component(final Key name) {
      return new ComponentTranslationStore(Objects.requireNonNull(name, "name"));
   }

   static TranslationStore.StringBased<MessageFormat> messageFormat(final Key name) {
      return new MessageFormatTranslationStore(Objects.requireNonNull(name, "name"));
   }

   boolean contains(final String key);

   boolean contains(final String key, final Locale locale);

   @Override
   default boolean canTranslate(final String key, final Locale locale) {
      return Translator.super.canTranslate(key, locale);
   }

   void defaultLocale(final Locale locale);

   void register(final String key, final Locale locale, final T translation);

   void registerAll(final Locale locale, final Map<String, T> translations);

   void registerAll(final Locale locale, final Set<String> keys, Function<String, T> function);

   void unregister(final String key);

   public interface StringBased<T> extends TranslationStore<T> {
      void registerAll(final Locale locale, final Path path, final boolean escapeSingleQuotes);

      void registerAll(final Locale locale, final ResourceBundle bundle, final boolean escapeSingleQuotes);
   }
}

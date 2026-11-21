package net.kyori.adventure.translation;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.util.TriState;
import org.jspecify.annotations.Nullable;

public abstract class AbstractTranslationStore<T> implements TranslationStore<T> {
   private final Key name;
   private final Map<String, AbstractTranslationStore<T>.Translation> translations = new ConcurrentHashMap<>();
   private volatile Locale defaultLocale = Locale.US;

   protected AbstractTranslationStore(final Key name) {
      this.name = Objects.requireNonNull(name, "name");
   }

   @Nullable
   protected T translationValue(final String key, final Locale locale) {
      AbstractTranslationStore<T>.Translation translation = this.translations.get(Objects.requireNonNull(key, "key"));
      return (T)(translation == null ? null : translation.translate(Objects.requireNonNull(locale, "locale")));
   }

   @Override
   public final boolean contains(final String key) {
      return this.translations.containsKey(key);
   }

   @Override
   public final boolean contains(final String key, final Locale locale) {
      AbstractTranslationStore<T>.Translation translation = this.translations.get(Objects.requireNonNull(key, "key"));
      return translation == null ? false : translation.translations.get(Objects.requireNonNull(locale, "locale")) != null;
   }

   @Override
   public final boolean canTranslate(final String key, final Locale locale) {
      AbstractTranslationStore<T>.Translation translation = this.translations.get(Objects.requireNonNull(key, "key"));
      return translation == null ? false : translation.translate(Objects.requireNonNull(locale, "locale")) != null;
   }

   @Override
   public final void defaultLocale(final Locale locale) {
      this.defaultLocale = Objects.requireNonNull(locale, "locale");
   }

   @Override
   public final void register(final String key, final Locale locale, final T translation) {
      this.translations.computeIfAbsent(key, x$0 -> new AbstractTranslationStore.Translation(x$0)).register(locale, translation);
   }

   @Override
   public final void registerAll(final Locale locale, final Map<String, T> translations) {
      this.registerAll(locale, translations.keySet(), translations::get);
   }

   @Override
   public final void registerAll(final Locale locale, final Set<String> keys, final Function<String, T> function) {
      IllegalArgumentException firstError = null;
      int errorCount = 0;

      for (String key : keys) {
         try {
            this.register(key, locale, function.apply(key));
         } catch (IllegalArgumentException var9) {
            if (firstError == null) {
               firstError = var9;
            }

            errorCount++;
         }
      }

      if (firstError != null) {
         if (errorCount == 1) {
            throw firstError;
         }

         if (errorCount > 1) {
            throw new IllegalArgumentException(String.format("Invalid key (and %d more)", errorCount - 1), firstError);
         }
      }
   }

   @Override
   public final void unregister(final String key) {
      this.translations.remove(key);
   }

   @Override
   public final Key name() {
      return this.name;
   }

   @Override
   public final TriState hasAnyTranslations() {
      return TriState.byBoolean(!this.translations.isEmpty());
   }

   @Override
   public final boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else {
         return other instanceof AbstractTranslationStore<?> that ? this.name.equals(that.name) : false;
      }
   }

   @Override
   public final int hashCode() {
      return this.name.hashCode();
   }

   @Override
   public String toString() {
      return "AbstractTranslationStore{name=" + this.name + ", translations=" + this.translations + ", defaultLocale=" + this.defaultLocale + "}";
   }

   public abstract static class StringBased<T> extends AbstractTranslationStore<T> implements TranslationStore.StringBased<T> {
      private static final Pattern SINGLE_QUOTE_PATTERN = Pattern.compile("'");

      protected StringBased(final Key name) {
         super(name);
      }

      protected abstract T parse(final String string, final Locale locale);

      @Override
      public final void registerAll(final Locale locale, final Path path, final boolean escapeSingleQuotes) {
         try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            this.registerAll(locale, new PropertyResourceBundle(reader), escapeSingleQuotes);
         } catch (IOException var9) {
         }
      }

      @Override
      public final void registerAll(final Locale locale, final ResourceBundle bundle, final boolean escapeSingleQuotes) {
         this.registerAll(locale, bundle.keySet(), key -> {
            String format = bundle.getString(key);
            return this.parse(escapeSingleQuotes ? SINGLE_QUOTE_PATTERN.matcher(format).replaceAll("''") : format, locale);
         });
      }
   }

   private final class Translation {
      private final String key;
      private final Map<Locale, T> translations;

      private Translation(final String key) {
         this.key = Objects.requireNonNull(key, "key");
         this.translations = new ConcurrentHashMap<>();
      }

      @Nullable
      private T translate(final Locale locale) {
         T format = this.translations.get(Objects.requireNonNull(locale, "locale"));
         if (format == null) {
            format = this.translations.get(Locale.of(locale.getLanguage()));
            if (format == null) {
               format = this.translations.get(AbstractTranslationStore.this.defaultLocale);
               if (format == null) {
                  format = this.translations.get(TranslationLocales.global());
               }
            }
         }

         return format;
      }

      private void register(final Locale locale, final T translation) {
         if (this.translations.putIfAbsent(Objects.requireNonNull(locale, "locale"), Objects.requireNonNull(translation, "translation")) != null) {
            throw new IllegalArgumentException(String.format("Translation already exists: %s for %s", this.key, locale));
         }
      }

      @Override
      public boolean equals(final Object other) {
         if (this == other) {
            return true;
         } else {
            return !(other instanceof AbstractTranslationStore<?>.Translation that)
               ? false
               : this.key.equals(that.key) && this.translations.equals(that.translations);
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.key, this.translations);
      }

      @Override
      public String toString() {
         return "Translation{key='" + this.key + "', translations=" + this.translations + "}";
      }
   }
}

package de.theredend2000.advancedhunt.lib.kyori.adventure.translation;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TranslatableComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import de.theredend2000.advancedhunt.lib.kyori.adventure.util.TriState;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

final class GlobalTranslatorImpl implements GlobalTranslator {
   private static final Key NAME = Key.key("adventure", "global");
   static final GlobalTranslatorImpl INSTANCE = new GlobalTranslatorImpl();
   final TranslatableComponentRenderer<Locale> renderer = TranslatableComponentRenderer.usingTranslationSource(this);
   private final Set<Translator> sources = Collections.newSetFromMap(new ConcurrentHashMap<>());

   private GlobalTranslatorImpl() {
   }

   @Override
   public Key name() {
      return NAME;
   }

   @Override
   public Iterable<? extends Translator> sources() {
      return Set.copyOf(this.sources);
   }

   @Override
   public boolean addSource(final Translator source) {
      Objects.requireNonNull(source, "source");
      if (source == this) {
         throw new IllegalArgumentException("GlobalTranslationSource");
      } else {
         return this.sources.add(source);
      }
   }

   @Override
   public boolean removeSource(final Translator source) {
      Objects.requireNonNull(source, "source");
      return this.sources.remove(source);
   }

   @Override
   public TriState hasAnyTranslations() {
      return !this.sources.isEmpty() ? TriState.TRUE : TriState.FALSE;
   }

   @Override
   public boolean canTranslate(final String key, final Locale locale) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(locale, "locale");

      for (Translator source : this.sources) {
         if (source.canTranslate(key, locale)) {
            return true;
         }
      }

      return false;
   }

   @Nullable
   @Override
   public MessageFormat translate(final String key, final Locale locale) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(locale, "locale");

      for (Translator source : this.sources) {
         MessageFormat translation = source.translate(key, locale);
         if (translation != null) {
            return translation;
         }
      }

      return null;
   }

   @Nullable
   @Override
   public Component translate(final TranslatableComponent component, final Locale locale) {
      Objects.requireNonNull(component, "component");
      Objects.requireNonNull(locale, "locale");

      for (Translator source : this.sources) {
         Component translation = source.translate(component, locale);
         if (translation != null) {
            return translation;
         }
      }

      return null;
   }

   @Override
   public String toString() {
      return "GlobalTranslatorImpl{sources=" + this.sources + "}";
   }
}

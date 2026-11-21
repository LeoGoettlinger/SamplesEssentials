package de.theredend2000.advancedhunt.lib.kyori.adventure.translation;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import java.util.Locale;

public interface GlobalTranslator extends Translator {
   static GlobalTranslator translator() {
      return GlobalTranslatorImpl.INSTANCE;
   }

   static TranslatableComponentRenderer<Locale> renderer() {
      return GlobalTranslatorImpl.INSTANCE.renderer;
   }

   static Component render(final Component component, final Locale locale) {
      return renderer().render(component, locale);
   }

   Iterable<? extends Translator> sources();

   boolean addSource(final Translator source);

   boolean removeSource(final Translator source);
}

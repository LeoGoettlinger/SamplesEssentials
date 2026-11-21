package net.kyori.adventure.translation;

import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;

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

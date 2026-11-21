package de.theredend2000.advancedhunt.lib.kyori.adventure.translation;

import de.theredend2000.advancedhunt.lib.kyori.adventure.key.Key;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TranslatableComponent;
import java.text.MessageFormat;
import java.util.Locale;
import org.jspecify.annotations.Nullable;

final class ComponentTranslationStore extends AbstractTranslationStore<Component> {
   ComponentTranslationStore(final Key name) {
      super(name);
   }

   @Nullable
   @Override
   public MessageFormat translate(final String key, final Locale locale) {
      return null;
   }

   @Nullable
   @Override
   public Component translate(final TranslatableComponent component, final Locale locale) {
      Component translatedComponent = this.translationValue(component.key(), locale);
      return translatedComponent == null ? null : translatedComponent.append(component.children());
   }
}

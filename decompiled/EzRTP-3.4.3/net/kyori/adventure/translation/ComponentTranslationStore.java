package net.kyori.adventure.translation;

import java.text.MessageFormat;
import java.util.Locale;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
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

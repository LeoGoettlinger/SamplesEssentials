package xyz.lychee.lagfixer.libs.kyori.adventure.translation;

import java.text.MessageFormat;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.key.Key;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.TranslatableComponent;

final class ComponentTranslationStore extends AbstractTranslationStore<Component> {
   ComponentTranslationStore(@NotNull final Key name) {
      super(name);
   }

   @Nullable
   @Override
   public MessageFormat translate(@NotNull final String key, @NotNull final Locale locale) {
      return null;
   }

   @Nullable
   @Override
   public Component translate(@NotNull final TranslatableComponent component, @NotNull final Locale locale) {
      Component translatedComponent = this.translationValue(component.key(), locale);
      return translatedComponent == null ? null : translatedComponent.append(component.children());
   }
}

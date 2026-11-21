package net.kyori.adventure.translation;

import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.examination.Examinable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface GlobalTranslator extends Translator, Examinable {
   @NotNull
   static GlobalTranslator translator() {
      return GlobalTranslatorImpl.INSTANCE;
   }

   @Deprecated
   @ApiStatus.ScheduledForRemoval(
      inVersion = "5.0.0"
   )
   @NotNull
   static GlobalTranslator get() {
      return GlobalTranslatorImpl.INSTANCE;
   }

   @NotNull
   static TranslatableComponentRenderer<Locale> renderer() {
      return GlobalTranslatorImpl.INSTANCE.renderer;
   }

   @NotNull
   static Component render(@NotNull final Component component, @NotNull final Locale locale) {
      return renderer().render(component, locale);
   }

   @NotNull
   Iterable<? extends Translator> sources();

   boolean addSource(@NotNull final Translator source);

   boolean removeSource(@NotNull final Translator source);
}

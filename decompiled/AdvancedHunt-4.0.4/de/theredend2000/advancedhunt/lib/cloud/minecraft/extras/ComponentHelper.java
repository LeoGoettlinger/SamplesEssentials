package de.theredend2000.advancedhunt.lib.cloud.minecraft.extras;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.TextComponent;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextColor;
import de.theredend2000.advancedhunt.lib.kyori.adventure.translation.GlobalTranslator;
import java.util.Locale;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.NonNull;

final class ComponentHelper {
   public static final Pattern SPECIAL_CHARACTERS_PATTERN = Pattern.compile("[^\\s\\w\\-]");

   private ComponentHelper() {
   }

   @NonNull
   public static Component highlight(@NonNull final Component component, @NonNull final TextColor highlightColor) {
      return component.replaceText(config -> {
         config.match(SPECIAL_CHARACTERS_PATTERN);
         config.replacement(match -> match.color(highlightColor));
      });
   }

   @NonNull
   public static Component repeat(@NonNull final Component component, final int repetitions) {
      TextComponent.Builder builder = Component.text();

      for (int i = 0; i < repetitions; i++) {
         builder.append(component);
      }

      return builder.build();
   }

   public static int length(@NonNull final Component component) {
      int length = 0;
      if (component instanceof TextComponent) {
         length += ((TextComponent)component).content().length();
      }

      Component translated = GlobalTranslator.render(component, Locale.getDefault());

      for (Component child : translated.children()) {
         length += length(child);
      }

      return length;
   }
}

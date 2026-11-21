package de.theredend2000.advancedhunt.lib.kyori.adventure.text.serializer.legacy;

import de.theredend2000.advancedhunt.lib.kyori.adventure.text.format.TextFormat;
import java.util.List;
import org.jspecify.annotations.Nullable;

record CharacterAndFormatImpl(char character, @Nullable TextFormat format, boolean caseInsensitive) implements CharacterAndFormat {
   static final class Defaults {
      static final List<CharacterAndFormat> DEFAULTS = createDefaults();

      private Defaults() {
      }

      static List<CharacterAndFormat> createDefaults() {
         return List.of(
            CharacterAndFormat.BLACK,
            CharacterAndFormat.DARK_BLUE,
            CharacterAndFormat.DARK_GREEN,
            CharacterAndFormat.DARK_AQUA,
            CharacterAndFormat.DARK_RED,
            CharacterAndFormat.DARK_PURPLE,
            CharacterAndFormat.GOLD,
            CharacterAndFormat.GRAY,
            CharacterAndFormat.DARK_GRAY,
            CharacterAndFormat.BLUE,
            CharacterAndFormat.GREEN,
            CharacterAndFormat.AQUA,
            CharacterAndFormat.RED,
            CharacterAndFormat.LIGHT_PURPLE,
            CharacterAndFormat.YELLOW,
            CharacterAndFormat.WHITE,
            CharacterAndFormat.OBFUSCATED,
            CharacterAndFormat.BOLD,
            CharacterAndFormat.STRIKETHROUGH,
            CharacterAndFormat.UNDERLINED,
            CharacterAndFormat.ITALIC,
            CharacterAndFormat.RESET
         );
      }
   }
}

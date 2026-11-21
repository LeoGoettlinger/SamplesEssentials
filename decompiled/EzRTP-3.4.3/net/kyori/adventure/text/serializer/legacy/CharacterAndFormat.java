package net.kyori.adventure.text.serializer.legacy;

import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextFormat;
import org.jetbrains.annotations.Unmodifiable;
import org.jspecify.annotations.Nullable;

public sealed interface CharacterAndFormat permits CharacterAndFormatImpl {
   CharacterAndFormat BLACK = characterAndFormat('0', NamedTextColor.BLACK, true);
   CharacterAndFormat DARK_BLUE = characterAndFormat('1', NamedTextColor.DARK_BLUE, true);
   CharacterAndFormat DARK_GREEN = characterAndFormat('2', NamedTextColor.DARK_GREEN, true);
   CharacterAndFormat DARK_AQUA = characterAndFormat('3', NamedTextColor.DARK_AQUA, true);
   CharacterAndFormat DARK_RED = characterAndFormat('4', NamedTextColor.DARK_RED, true);
   CharacterAndFormat DARK_PURPLE = characterAndFormat('5', NamedTextColor.DARK_PURPLE, true);
   CharacterAndFormat GOLD = characterAndFormat('6', NamedTextColor.GOLD, true);
   CharacterAndFormat GRAY = characterAndFormat('7', NamedTextColor.GRAY, true);
   CharacterAndFormat DARK_GRAY = characterAndFormat('8', NamedTextColor.DARK_GRAY, true);
   CharacterAndFormat BLUE = characterAndFormat('9', NamedTextColor.BLUE, true);
   CharacterAndFormat GREEN = characterAndFormat('a', NamedTextColor.GREEN, true);
   CharacterAndFormat AQUA = characterAndFormat('b', NamedTextColor.AQUA, true);
   CharacterAndFormat RED = characterAndFormat('c', NamedTextColor.RED, true);
   CharacterAndFormat LIGHT_PURPLE = characterAndFormat('d', NamedTextColor.LIGHT_PURPLE, true);
   CharacterAndFormat YELLOW = characterAndFormat('e', NamedTextColor.YELLOW, true);
   CharacterAndFormat WHITE = characterAndFormat('f', NamedTextColor.WHITE, true);
   CharacterAndFormat OBFUSCATED = characterAndFormat('k', TextDecoration.OBFUSCATED, true);
   CharacterAndFormat BOLD = characterAndFormat('l', TextDecoration.BOLD, true);
   CharacterAndFormat STRIKETHROUGH = characterAndFormat('m', TextDecoration.STRIKETHROUGH, true);
   CharacterAndFormat UNDERLINED = characterAndFormat('n', TextDecoration.UNDERLINED, true);
   CharacterAndFormat ITALIC = characterAndFormat('o', TextDecoration.ITALIC, true);
   CharacterAndFormat RESET = characterAndReset('r', true);

   static CharacterAndFormat characterAndReset(final char character) {
      return characterAndReset(character, false);
   }

   static CharacterAndFormat characterAndReset(final char character, final boolean caseInsensitive) {
      return new CharacterAndFormatImpl(character, null, caseInsensitive);
   }

   static CharacterAndFormat characterAndFormat(final char character, final TextFormat format) {
      return characterAndFormat(character, format, false);
   }

   static CharacterAndFormat characterAndFormat(final char character, final TextFormat format, final boolean caseInsensitive) {
      return new CharacterAndFormatImpl(character, Objects.requireNonNull(format, "format"), caseInsensitive);
   }

   @Unmodifiable
   static List<CharacterAndFormat> defaults() {
      return CharacterAndFormatImpl.Defaults.DEFAULTS;
   }

   char character();

   @Nullable
   TextFormat format();

   boolean caseInsensitive();
}

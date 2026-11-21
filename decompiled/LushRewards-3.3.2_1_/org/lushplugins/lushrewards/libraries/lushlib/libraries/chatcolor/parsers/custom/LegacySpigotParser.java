package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;

public class LegacySpigotParser implements Parser {
   public static final LegacySpigotParser INSTANCE = new LegacySpigotParser();

   private LegacySpigotParser() {
   }

   @Override
   public String getType() {
      return "color";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return switch (outputType) {
         case SPIGOT -> string;
         case MINI_MESSAGE -> string.replace("§", "&")
            .replace("&0", "<reset><black>")
            .replace("&1", "<reset><dark_blue>")
            .replace("&2", "<reset><dark_green>")
            .replace("&3", "<reset><dark_aqua>")
            .replace("&4", "<reset><dark_red>")
            .replace("&5", "<reset><dark_purple>")
            .replace("&6", "<reset><gold>")
            .replace("&7", "<reset><grey>")
            .replace("&8", "<reset><dark_grey>")
            .replace("&9", "<reset><blue>")
            .replace("&a", "<reset><green>")
            .replace("&b", "<reset><aqua>")
            .replace("&c", "<reset><red>")
            .replace("&d", "<reset><light_purple>")
            .replace("&e", "<reset><yellow>")
            .replace("&f", "<reset><white>")
            .replace("&m", "<strikethrough>")
            .replace("&k", "<obfuscated>")
            .replace("&n", "<underlined>")
            .replace("&o", "<italic>")
            .replace("&l", "<bold>")
            .replace("&r", "<reset>");
      };
   }
}

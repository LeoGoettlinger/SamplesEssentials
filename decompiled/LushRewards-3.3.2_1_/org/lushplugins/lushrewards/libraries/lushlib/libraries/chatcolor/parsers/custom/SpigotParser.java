package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;

public class SpigotParser implements Parser {
   public static final SpigotParser INSTANCE = new SpigotParser();

   private SpigotParser() {
   }

   @Override
   public String getType() {
      return "color";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return switch (outputType) {
         case SPIGOT -> ChatColor.translateAlternateColorCodes('&', string);
         case MINI_MESSAGE -> string;
      };
   }
}

package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;

public class PlaceholderAPIParser implements Parser {
   public static final PlaceholderAPIParser INSTANCE = new PlaceholderAPIParser();

   private PlaceholderAPIParser() {
   }

   @Override
   public String getType() {
      return "placeholder";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return PlaceholderAPI.setPlaceholders(null, string);
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType, Player player) {
      return PlaceholderAPI.setPlaceholders(player, string);
   }
}

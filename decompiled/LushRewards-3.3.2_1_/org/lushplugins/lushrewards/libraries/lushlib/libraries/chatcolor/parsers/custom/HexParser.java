package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;

public class HexParser implements Parser {
   public static final HexParser INSTANCE = new HexParser();
   private static final Pattern HEX_PATTERN = Pattern.compile("&(#[a-fA-F0-9]{6})");

   private HexParser() {
   }

   @Override
   public String getType() {
      return "color";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      Matcher match = HEX_PATTERN.matcher(string);

      return switch (outputType) {
         case SPIGOT -> match.replaceAll(result -> ChatColor.of(result.group(1)).toString());
         case MINI_MESSAGE -> match.replaceAll("<reset><$1>");
      };
   }
}

package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.custom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers.Parser;

public class LegacyHexParser implements Parser {
   public static final LegacyHexParser INSTANCE = new LegacyHexParser();
   private static final Pattern HEX_PATTERN = Pattern.compile(
      "[§&]x[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])"
   );

   private LegacyHexParser() {
   }

   @Override
   public String getType() {
      return "color";
   }

   @Override
   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType) {
      return switch (outputType) {
         case SPIGOT -> string;
         case MINI_MESSAGE -> {
            Matcher match = HEX_PATTERN.matcher(string);
            yield match.replaceAll("<reset><#$1$2$3$4$5$6>");
         }
      };
   }
}

package de.oliver.fancynpcs.libs.chatcolorhandler.common.parser;

import java.util.regex.Pattern;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class LegacyHexParser implements Parser {
   public static final LegacyHexParser INSTANCE = new LegacyHexParser(70);
   private static final Pattern HEX_PATTERN = Pattern.compile(
      "[§&]x[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])[§&]([a-fA-F0-9])"
   );
   private final int priority;

   public LegacyHexParser(int priority) {
      this.priority = priority;
   }

   @Override
   public ParserType getType() {
      return ParserType.COLOR;
   }

   @Override
   public int getPriority() {
      return this.priority;
   }

   @Override
   public String parseString(String string, @Nullable Player player) {
      return HEX_PATTERN.matcher(string).replaceAll("<reset><#$1$2$3$4$5$6>");
   }
}

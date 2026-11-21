package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface Parser {
   String getType();

   String parseString(@NotNull String var1, @NotNull Parser.OutputType var2);

   default String parseString(@NotNull String string, @NotNull Parser.OutputType outputType, Player player) {
      return this.parseString(string, outputType);
   }

   public static enum OutputType {
      SPIGOT,
      MINI_MESSAGE;
   }
}

package de.oliver.fancynpcs.libs.chatcolorhandler.common.parser;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public interface Parser {
   @Nullable
   ParserType getType();

   int getPriority();

   String parseString(String var1, @Nullable Player var2);
}

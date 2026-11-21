package com.magmaguy.elitemobs.magmacore.command.arguments;

import java.util.List;
import org.bukkit.command.CommandSender;

public interface ICommandArgument {
   String hint();

   boolean matchesInput(String var1);

   List<String> literals();

   List<String> getSuggestions(CommandSender var1, String var2);

   boolean isLiteral();

   default boolean isOptional() {
      return false;
   }

   default boolean isVarargs() {
      return false;
   }
}

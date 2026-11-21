package dev.cwhead.GravesX.module.command;

import java.util.Collections;
import java.util.List;
import org.bukkit.command.CommandExecutor;

public interface GravesXModuleCommand extends CommandExecutor {
   default String getName() {
      return null;
   }

   default String getDescription() {
      return "";
   }

   default String getUsage() {
      return null;
   }

   default String getPermission() {
      return null;
   }

   default List<String> getAliases() {
      return Collections.emptyList();
   }
}

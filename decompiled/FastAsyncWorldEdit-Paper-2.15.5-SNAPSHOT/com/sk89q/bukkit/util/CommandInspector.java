package com.sk89q.bukkit.util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface CommandInspector {
   String getShortText(Command command);

   String getFullText(Command command);

   boolean testPermission(CommandSender sender, Command command);
}

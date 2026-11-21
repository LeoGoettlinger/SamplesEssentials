package dev.cwhead.GravesX.module.command;

import dev.cwhead.GravesX.module.ModuleContext;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public interface GravesXModuleTabCompleter extends TabCompleter {
   default void onRegister(ModuleContext ctx, PluginCommand command) {
   }

   default void onUnregister() {
   }
}

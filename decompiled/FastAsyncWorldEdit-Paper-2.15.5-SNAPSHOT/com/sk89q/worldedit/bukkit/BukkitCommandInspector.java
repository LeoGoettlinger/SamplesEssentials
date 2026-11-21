package com.sk89q.worldedit.bukkit;

import com.google.common.base.Preconditions;
import com.sk89q.bukkit.util.CommandInspector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import java.util.Optional;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.CommandSender;
import org.enginehub.piston.Command;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.inject.InjectedValueStore;
import org.enginehub.piston.inject.Key;
import org.enginehub.piston.inject.MapBackedValueStore;

class BukkitCommandInspector implements CommandInspector {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final WorldEditPlugin plugin;
   private final CommandManager dispatcher;

   BukkitCommandInspector(WorldEditPlugin plugin, CommandManager dispatcher) {
      Preconditions.checkNotNull(plugin);
      Preconditions.checkNotNull(dispatcher);
      this.plugin = plugin;
      this.dispatcher = dispatcher;
   }

   @Override
   public String getShortText(org.bukkit.command.Command command) {
      Optional<Command> mapping = this.dispatcher.getCommand(command.getName());
      if (mapping.isPresent()) {
         return WorldEditText.reduceToText(mapping.get().getDescription(), WorldEdit.getInstance().getConfiguration().defaultLocale);
      } else {
         LOGGER.warn("BukkitCommandInspector doesn't know how about the command '" + command + "'");
         return "Help text not available";
      }
   }

   @Override
   public String getFullText(org.bukkit.command.Command command) {
      Optional<Command> mapping = this.dispatcher.getCommand(command.getName());
      if (mapping.isPresent()) {
         return WorldEditText.reduceToText(mapping.get().getFullHelp(), WorldEdit.getInstance().getConfiguration().defaultLocale);
      } else {
         LOGGER.warn("BukkitCommandInspector doesn't know how about the command '" + command + "'");
         return "Help text not available";
      }
   }

   @Override
   public boolean testPermission(CommandSender sender, org.bukkit.command.Command command) {
      Optional<Command> mapping = this.dispatcher.getCommand(command.getName());
      if (mapping.isPresent()) {
         InjectedValueStore store = MapBackedValueStore.create();
         store.injectValue(Key.of(Actor.class), context -> Optional.of(this.plugin.wrapCommandSender(sender)));
         return mapping.get().getCondition().satisfied(store);
      } else {
         LOGGER.warn("BukkitCommandInspector doesn't know how about the command '" + command + "'");
         return false;
      }
   }
}

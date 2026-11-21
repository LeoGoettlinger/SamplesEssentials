package com.sk89q.bukkit.util;

import com.sk89q.util.ReflectionUtil;
import com.sk89q.worldedit.bukkit.paperlib.PaperLib;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

public class CommandRegistration {
   protected final Plugin plugin;
   protected final CommandExecutor executor;
   private CommandMap serverCommandMap;
   private CommandMap fallbackCommands;

   public CommandRegistration(Plugin plugin) {
      this(plugin, plugin);
   }

   public CommandRegistration(Plugin plugin, CommandExecutor executor) {
      this.plugin = plugin;
      this.executor = executor;
   }

   public Plugin getCommandOwner(String label) {
      if (this.serverCommandMap == null) {
         return null;
      } else {
         Command command = this.serverCommandMap.getCommand(label);
         return command instanceof PluginIdentifiableCommand ? ((PluginIdentifiableCommand)command).getPlugin() : null;
      }
   }

   public boolean register(List<CommandInfo> registered) {
      CommandMap commandMap = this.getCommandMap();
      if (registered != null && commandMap != null) {
         for (CommandInfo command : registered) {
            DynamicPluginCommand cmd = new DynamicPluginCommand(
               command.getAliases(), command.getDesc(), command.getUsage(), this.executor, command.getRegisteredWith(), this.plugin
            );
            cmd.setPermissions(command.getPermissions());
            commandMap.register(this.plugin.getDescription().getName(), cmd);
         }

         return true;
      } else {
         return false;
      }
   }

   public CommandMap getCommandMap() {
      if (this.serverCommandMap != null) {
         return this.serverCommandMap;
      } else if (this.fallbackCommands != null) {
         return this.fallbackCommands;
      } else {
         CommandMap commandMap = PaperLib.isPaper()
            ? Bukkit.getCommandMap()
            : ReflectionUtil.getField(this.plugin.getServer().getPluginManager(), "commandMap");
         if (commandMap == null) {
            Bukkit.getServer().getLogger().severe(this.plugin.getDescription().getName() + ": Could not retrieve server CommandMap");
            throw new IllegalStateException("Failed to retrieve command map, make sure you are running supported server software");
         } else {
            this.serverCommandMap = commandMap;
            return commandMap;
         }
      }
   }

   public boolean unregisterCommands() {
      CommandMap commandMap = this.getCommandMap();
      List<String> toRemove = new ArrayList<>();
      Map<String, Command> knownCommands = ReflectionUtil.getField(commandMap, "knownCommands");
      Set<String> aliases = ReflectionUtil.getField(commandMap, "aliases");
      if (knownCommands != null && aliases != null) {
         Iterator<Command> i = knownCommands.values().iterator();

         while (i.hasNext()) {
            Command cmd = i.next();
            if (cmd instanceof DynamicPluginCommand && ((DynamicPluginCommand)cmd).getOwner().equals(this.executor)) {
               i.remove();

               for (String alias : cmd.getAliases()) {
                  Command aliasCmd = knownCommands.get(alias);
                  if (cmd.equals(aliasCmd)) {
                     aliases.remove(alias);
                     toRemove.add(alias);
                  }
               }
            }
         }

         for (String string : toRemove) {
            knownCommands.remove(string);
         }

         return true;
      } else {
         return false;
      }
   }

   static {
      Bukkit.getServer().getHelpMap().registerHelpTopicFactory(DynamicPluginCommand.class, new DynamicPluginCommandHelpTopic.Factory());
   }
}

package com.lishid.openinv.command;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.util.InventoryManager;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.PlayerLoader;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.lang.LanguageManager;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OpenInvCommand extends PlayerLookupCommand {
   @NotNull
   private final InventoryManager manager;
   private final Map<Player, String> openInvHistory = new WeakHashMap<>();
   private final Map<Player, String> openEnderHistory = new WeakHashMap<>();

   public OpenInvCommand(
      @NotNull OpenInv plugin, @NotNull Config config, @NotNull InventoryManager manager, @NotNull LanguageManager lang, @NotNull PlayerLoader playerLoader
   ) {
      super(plugin, lang, config, playerLoader);
      this.manager = manager;
   }

   @Override
   protected boolean isAccessInventory(@NotNull Command command) {
      return command.getName().equals("openinv");
   }

   @Nullable
   @Override
   protected String getTargetIdentifer(@NotNull CommandSender sender, @NotNull Command command, @Nullable String argument, boolean accessInv) {
      if (!accessInv || argument == null || !argument.equalsIgnoreCase("help") && !argument.equals("?")) {
         if (sender instanceof Player player) {
            if (argument == null) {
               return this.config.doesNoArgsOpenSelf()
                  ? player.getUniqueId().toString()
                  : (accessInv ? this.openInvHistory : this.openEnderHistory).computeIfAbsent(player, localPlayer -> localPlayer.getUniqueId().toString());
            } else {
               if (!this.config.doesNoArgsOpenSelf()) {
                  (accessInv ? this.openInvHistory : this.openEnderHistory).put(player, argument);
               }

               return argument;
            }
         } else {
            this.lang.sendMessage(sender, "messages.error.consoleUnsupported");
            return null;
         }
      } else {
         this.showHelp(sender);
         return null;
      }
   }

   private void showHelp(@NotNull CommandSender sender) {
      for (String commandName : this.plugin.getDescription().getCommands().keySet()) {
         PluginCommand command = this.plugin.getCommand(commandName);
         if (command != null && command.testPermissionSilent(sender)) {
            sender.sendMessage(command.getUsage().replace("<command>", commandName));
            List<String> aliases = command.getAliases();
            if (!aliases.isEmpty()) {
               StringJoiner aliasJoiner = new StringJoiner(", ", "   (aliases: ", ")");

               for (String alias : aliases) {
                  aliasJoiner.add(alias);
               }

               sender.sendMessage(aliasJoiner.toString());
            }
         }
      }
   }

   @Nullable
   @Override
   protected OfflinePlayer getTarget(@NotNull String identifier) {
      return this.playerLoader.match(identifier);
   }

   @Override
   protected boolean deniedCommand(@NotNull CommandSender sender, @NotNull Player onlineTarget, boolean accessInv) {
      if (onlineTarget.equals(sender)) {
         if (!(accessInv ? Permissions.INVENTORY_OPEN_SELF : Permissions.ENDERCHEST_OPEN_SELF).hasPermission(sender)) {
            this.lang.sendMessage(sender, "messages.error.permissionOpenSelf");
            return true;
         }
      } else if (!(accessInv ? Permissions.INVENTORY_OPEN_OTHER : Permissions.ENDERCHEST_OPEN_OTHER).hasPermission(sender)) {
         this.lang.sendMessage(sender, "messages.error.permissionOpenOther");
         return true;
      }

      return false;
   }

   @Override
   protected void handle(@NotNull CommandSender sender, @NotNull PlayerLookupCommand.PlayerAccess playerAccess, boolean accessInv, @NotNull String[] args) {
      Player player = (Player)sender;
      Player target = playerAccess.player();
      if (!this.config.doesNoArgsOpenSelf()) {
         (accessInv ? this.openInvHistory : this.openEnderHistory).put(player, target.getUniqueId().toString());
      }

      ISpecialInventory inv;
      try {
         inv = (ISpecialInventory)(accessInv ? this.manager.getInventory(target) : this.manager.getEnderChest(target));
      } catch (Exception var9) {
         this.lang.sendMessage(player, "messages.error.commandException");
         this.plugin.getLogger().log(Level.WARNING, "Unable to create ISpecialInventory", (Throwable)var9);
         return;
      }

      this.plugin.openInventory(player, inv, playerAccess.viewOnly());
   }
}

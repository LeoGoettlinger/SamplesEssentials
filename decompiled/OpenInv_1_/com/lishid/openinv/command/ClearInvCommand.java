package com.lishid.openinv.command;

import com.lishid.openinv.OpenInv;
import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.util.InventoryManager;
import com.lishid.openinv.util.Permissions;
import com.lishid.openinv.util.PlayerLoader;
import com.lishid.openinv.util.config.Config;
import com.lishid.openinv.util.lang.LanguageManager;
import com.lishid.openinv.util.lang.Replacement;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClearInvCommand extends PlayerLookupCommand {
   @NotNull
   private final InventoryManager manager;

   public ClearInvCommand(
      @NotNull OpenInv plugin, @NotNull Config config, @NotNull InventoryManager manager, @NotNull LanguageManager lang, @NotNull PlayerLoader playerLoader
   ) {
      super(plugin, lang, config, playerLoader);
      this.manager = manager;
   }

   @Override
   protected boolean isAccessInventory(@NotNull Command command) {
      return command.getName().equals("clearinv");
   }

   @Nullable
   @Override
   protected String getTargetIdentifer(@NotNull CommandSender sender, @NotNull Command command, @Nullable String argument, boolean accessInv) {
      if (!Permissions.CLEAR_OTHER.hasPermission(sender)) {
         if (sender instanceof Player player) {
            return player.getUniqueId().toString();
         } else {
            this.lang.sendMessage(sender, "messages.error.permissionOpenOther");
            return null;
         }
      } else if (argument != null) {
         return argument;
      } else if (sender instanceof Player player) {
         return player.getUniqueId().toString();
      } else {
         sender.sendMessage(command.getUsage());
         return null;
      }
   }

   @Nullable
   @Override
   protected OfflinePlayer getTarget(@NotNull String identifier) {
      return this.playerLoader.matchExact(identifier);
   }

   @Override
   protected boolean deniedCommand(@NotNull CommandSender sender, @NotNull Player onlineTarget, boolean accessInv) {
      return onlineTarget.equals(sender) ? !Permissions.CLEAR_SELF.hasPermission(sender) : !Permissions.CLEAR_OTHER.hasPermission(sender);
   }

   @Override
   protected void handle(@NotNull CommandSender sender, @NotNull PlayerLookupCommand.PlayerAccess playerAccess, boolean accessInv, @NotNull String[] args) {
      Player onlineTarget = playerAccess.player();

      ISpecialInventory inv;
      try {
         inv = (ISpecialInventory)(accessInv ? this.manager.getInventory(onlineTarget) : this.manager.getEnderChest(onlineTarget));
      } catch (Exception var8) {
         this.lang.sendMessage(sender, "messages.error.commandException");
         this.plugin.getLogger().log(Level.WARNING, "Unable to create ISpecialInventory", (Throwable)var8);
         return;
      }

      inv.getBukkitInventory().clear();
      this.manager.save(onlineTarget.getUniqueId());
      this.lang
         .sendMessage(
            sender, accessInv ? "messages.info.clear.inventory" : "messages.info.clear.enderchest", new Replacement("%target%", onlineTarget.getDisplayName())
         );
   }

   @Override
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
      return !Permissions.CLEAR_OTHER.hasPermission(sender) ? List.of() : super.onTabComplete(sender, command, label, args);
   }
}

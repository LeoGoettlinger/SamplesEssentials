package com.Sw3ys.bigenderchest.commands;

import com.Sw3ys.bigenderchest.gui.SelectorGUI;
import com.Sw3ys.bigenderchest.manager.EnderChestManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ECSeeCommand implements CommandExecutor {
   private final SelectorGUI selector;
   private final EnderChestManager manager;

   public ECSeeCommand(SelectorGUI selector, EnderChestManager manager) {
      this.selector = selector;
      this.manager = manager;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (sender instanceof Player p) {
         if (!p.hasPermission("enderchest.admin")) {
            p.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
         } else if (args.length == 0) {
            this.selector.open(p, 0);
            return true;
         } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            if (!target.hasPlayedBefore() && !target.isOnline()) {
               p.sendMessage(ChatColor.RED + "That player has never joined.");
               return true;
            } else {
               this.manager.open(p, target.getUniqueId());
               return true;
            }
         }
      } else {
         return true;
      }
   }
}

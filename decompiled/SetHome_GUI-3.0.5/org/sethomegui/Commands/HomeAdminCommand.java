package org.sethomegui.Commands;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.Importers.HomeImporter;
import org.sethomegui.Utils.Utils;

public class HomeAdminCommand implements CommandExecutor {
   private final SetHomeGUI plugin;
   private final HomeImporter importer;

   public HomeAdminCommand(SetHomeGUI plugin) {
      this.plugin = plugin;
      this.importer = new HomeImporter(plugin);
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!sender.hasPermission("sethome.admin")) {
         String noPermissionMsg = this.plugin
            .getMainConfig()
            .getString("messages.admin.no-permission", "&#ef6603[SetHomeGUI] &cYou do not have permission to execute this command.");
         sender.sendMessage(Utils.color(noPermissionMsg));
         return true;
      } else if (args.length == 0) {
         if (sender instanceof Player) {
            Utils.sendUsage((Player)sender, "homeadmin gui", this.plugin);
         } else {
            String cmdUsage = this.plugin.getMainConfig().getString("messages.admin.command-usage", "&cUsage: /homeadmin <gui|view|import|reload|version>");
            sender.sendMessage(Utils.color(cmdUsage));
         }

         return true;
      } else if (args[0].equalsIgnoreCase("version")) {
         List<String> versionLines = this.plugin.getMainConfig().getStringList("messages.plugin-version");
         String currentVersion = this.plugin.getPluginMeta().getVersion();
         if (versionLines != null && !versionLines.isEmpty()) {
            for (String line : versionLines) {
               sender.sendMessage(Utils.color(line.replace("%version%", currentVersion)));
            }
         } else {
            sender.sendMessage(Utils.color("&7Plugin: &#ef6603SetHomeGUI &7| Version: &#ef6603" + currentVersion));
         }

         return true;
      } else if (args[0].equalsIgnoreCase("reload")) {
         Bukkit.getAsyncScheduler()
            .runNow(
               this.plugin,
               task -> {
                  try {
                     this.plugin.getMainConfig().reload();
                     this.plugin.getGuisConfig().reload();
                     if (this.plugin.getActionsConfig() != null) {
                        this.plugin.getActionsConfig().reload();
                     }

                     String reloadMsg = this.plugin
                        .getMainConfig()
                        .getString("messages.admin.reload-success", "&#ef6603[SetHomeGUI] &aAll configurations (config, actions, guis) successfully reloaded!");
                     sender.sendMessage(Utils.color(reloadMsg));
                  } catch (Exception var4x) {
                     sender.sendMessage(Utils.color("&c&#ef6603[SetHomeGUI] &cAn error occurred while reloading data files. Check console."));
                     this.plugin.getLogger().severe("Could not reload configuration files: " + var4x.getMessage());
                     var4x.printStackTrace();
                  }
               }
            );
         return true;
      } else if (args[0].equalsIgnoreCase("import")) {
         if (args.length < 2) {
            String importUsage = this.plugin
               .getMainConfig()
               .getString("messages.admin.import-usage", "&#ef6603[SetHomeGUI] &cUsage: /homeadmin import <Essentials|HuskHomes>");
            sender.sendMessage(Utils.color(importUsage));
            return true;
         } else {
            String source = args[1];
            String startMsg = this.plugin
               .getMainConfig()
               .getString("messages.admin.import-started", "&#ef6603[SetHomeGUI] &#f9a805Starting data import process asynchronously...");
            sender.sendMessage(Utils.color(startMsg));
            Bukkit.getAsyncScheduler().runNow(this.plugin, task -> {
               if (source.equalsIgnoreCase("Essentials")) {
                  this.importer.importHomesFromEssentialsForAllPlayers(sender);
               } else if (source.equalsIgnoreCase("HuskHomes")) {
                  this.importer.importHomesFromHuskHomesForAllPlayers(sender, "HuskHomes/HuskHomesData.db");
               } else {
                  sender.sendMessage(Utils.color("&cUnknown source. Please use 'Essentials' or 'HuskHomes'."));
               }
            });
            return true;
         }
      } else if (args[0].equalsIgnoreCase("gui")) {
         if (!(sender instanceof Player player)) {
            String onlyPlayersMsg = this.plugin
               .getMainConfig()
               .getString("messages.only-players", "&#ef6603[SetHomeGUI] &cError: Only players can execute this command.");
            sender.sendMessage(Utils.color(onlyPlayersMsg));
            return true;
         } else {
            player.closeInventory();
            this.plugin.getAdminGUIManager().setPage(player.getUniqueId(), 1);
            player.getScheduler().run(this.plugin, t -> this.plugin.getAdminGUIManager().openAdminMenu(player), null);
            return true;
         }
      } else if (!args[0].equalsIgnoreCase("view")) {
         return true;
      } else if (!(sender instanceof Player)) {
         String onlyPlayersMsg = this.plugin
            .getMainConfig()
            .getString("messages.only-players", "&#ef6603[SetHomeGUI] &cError: Only players can execute this command.");
         sender.sendMessage(Utils.color(onlyPlayersMsg));
         return true;
      } else if (args.length < 2) {
         String viewUsage = this.plugin.getMainConfig().getString("messages.admin.view-usage", "&#ef6603[SetHomeGUI] &cUsage: /homeadmin view <player>");
         sender.sendMessage(Utils.color(viewUsage));
         return true;
      } else {
         Player admin = (Player)sender;
         String targetName = args[1];
         OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);
         if (targetPlayer != null && (targetPlayer.hasPlayedBefore() || targetPlayer.isOnline())) {
            admin.closeInventory();
            admin.getScheduler()
               .run(this.plugin, task -> this.plugin.getAdminGUIManager().openAdminPlayerHomesMenu(admin, targetPlayer.getUniqueId(), 1), null);
            return true;
         } else {
            String unknownPlayerMsg = this.plugin
               .getMainConfig()
               .getString("messages.admin.view-unknown-player", "&#ef6603[SetHomeGUI] &cError: Player &f%player% &chas never played on this server.");
            admin.sendMessage(Utils.color(unknownPlayerMsg.replace("%player%", targetName)));
            return true;
         }
      }
   }
}

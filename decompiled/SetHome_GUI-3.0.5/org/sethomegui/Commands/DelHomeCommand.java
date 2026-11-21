package org.sethomegui.Commands;

import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sethomegui.SetHomeGUI;
import org.sethomegui.SetHomeGUI.libs.YamlDocument;
import org.sethomegui.Utils.Utils;

public class DelHomeCommand implements CommandExecutor {
   private final SetHomeGUI plugin;

   public DelHomeCommand(SetHomeGUI plugin) {
      this.plugin = plugin;
   }

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player player)) {
         String onlyPlayersMsg = this.plugin
            .getConfig()
            .getString("messages.only-players", "&#ef6603[SetHomeGUI] &cError: Only players can execute this command.");
         sender.sendMessage(Utils.color(onlyPlayersMsg));
         return true;
      } else if (args.length == 0) {
         Utils.sendUsage(player, "/delhome <home name>", this.plugin);
         return true;
      } else {
         StringBuilder builder = new StringBuilder();

         for (int i = 0; i < args.length; i++) {
            builder.append(args[i]);
            if (i < args.length - 1) {
               builder.append(" ");
            }
         }

         String homeName = builder.toString().trim();
         if (homeName.startsWith("\"") && homeName.endsWith("\"") && homeName.length() > 1) {
            homeName = homeName.substring(1, homeName.length() - 1);
         }

         YamlDocument playerFile = this.plugin.getHomeManager().getPlayerFile(player.getUniqueId());
         List<String> rawHomesList = playerFile != null ? playerFile.getStringList("homes") : null;
         if (rawHomesList != null && rawHomesList.contains(homeName)) {
            this.plugin.getGuiManager().openConfirmationGUI(player, homeName);
            this.plugin.getGuiManager().playConfiguredClickSound(player, "homes-gui");
            return true;
         } else {
            String errorMsg = this.plugin
               .getConfig()
               .getString("messages.home-action-messages.home-not-found", "&#ef6603[SetHomeGUI] &cError: You do not have a home named &f%name%&c.");
            player.sendMessage(Utils.color(errorMsg.replace("%name%", homeName)));
            return true;
         }
      }
   }
}

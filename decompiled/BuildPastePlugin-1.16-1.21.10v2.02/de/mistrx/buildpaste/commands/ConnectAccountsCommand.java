package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.firebase.Firebase;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConnectAccountsCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args_) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         Integer responseCode = 404;
         if (args_.length >= 1) {
            responseCode = Firebase.connectAccounts(player.getUniqueId().toString(), args_[0]);
         } else {
            responseCode = Firebase.connectAccounts(player.getUniqueId().toString());
         }

         if (responseCode == 200) {
            player.sendMessage("Accounts connected successfully");
         } else if (responseCode == 404) {
            player.sendMessage("Your email wasn't found in combination with your minecraft name :(");
         }
      }

      return false;
   }
}

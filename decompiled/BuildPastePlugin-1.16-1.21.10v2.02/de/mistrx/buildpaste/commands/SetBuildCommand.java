package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.firebase.Firebase;
import de.mistrx.buildpaste.util.Functions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetBuildCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args_) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         String uuid = player.getUniqueId().toString();
         if (args_.length >= 1) {
            Integer response = Firebase.setSelectedBuildingID(uuid, args_[0]);
            if (response == 1) {
               player.sendMessage("Build copied");
            } else if (response == 0) {
               Functions.sendNotConnectedMessage(player);
            }
         }
      }

      return false;
   }
}

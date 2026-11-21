package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.util.Functions;
import de.mistrx.buildpaste.util.Variables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class PasteCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args_) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         if (!Functions.checkPermissions(player, "paste")) {
            return false;
         }

         String firstarg = null;
         String secondarg = null;
         if (args_.length >= 1) {
            firstarg = args_[0];
         }

         if (args_.length >= 2) {
            secondarg = args_[1];
         }

         String[] args = new String[]{firstarg, secondarg};
         String direction = null;
         direction = Functions.getLookDirection(player.getLocation().getYaw());
         String response = Functions.setBuilding(
            player.getUniqueId().toString(),
            new Vector(Math.floor(player.getLocation().getX()), Math.floor(player.getLocation().getY()), Math.floor(player.getLocation().getZ())),
            direction,
            args,
            true
         );
         if (response != null) {
            if (response.equals("success")) {
               Vector size = Variables.GetBuildSize(player.getUniqueId().toString());
               Integer blocksCount = size.getBlockX() * size.getBlockY() * size.getBlockZ();
               player.sendMessage(blocksCount + " blocks were pasted successfully");
            } else if (response.equals("error")) {
               Functions.sendNotConnectedMessage(player);
            } else if (response.equals("error-404")) {
               player.sendMessage("This Build doesn't exist (404)");
            } else if (response.equals("random-error")) {
               player.sendMessage("A random error occurred. Maybe try again or report the issue");
            }
         }
      }

      return false;
   }
}

package de.mistrx.buildpaste.commands;

import com.google.gson.JsonObject;
import de.mistrx.buildpaste.util.Functions;
import de.mistrx.buildpaste.util.Variables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UndoPasteCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         String uuid = player.getUniqueId().toString();
         if (!Functions.checkPermissions(player, "paste")) {
            return false;
         }

         if (Variables.GetLastPos(uuid) != null) {
            Functions.pasteCurrentBuilding(
               uuid,
               Variables.GetLastPos(uuid),
               Variables.GetLastBlockSize(uuid),
               Variables.GetBuildDirection(uuid),
               Variables.GetLastBlockIDs(uuid),
               Variables.GetLastBlockData(uuid),
               new JsonObject(),
               Variables.GetLastPasteDirection(uuid),
               "nopastemodifier",
               true,
               false
            );
         } else {
            player.sendMessage("You can't undo what hasn't been done");
         }
      }

      return false;
   }
}

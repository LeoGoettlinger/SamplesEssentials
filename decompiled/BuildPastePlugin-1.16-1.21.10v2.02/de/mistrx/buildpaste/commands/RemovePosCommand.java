package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.util.Variables;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemovePosCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args_) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         String uuid = player.getUniqueId().toString();
         Variables.SetPos1(uuid, null);
         Variables.SetPos2(uuid, null);
         player.sendMessage("All positions were removed");
      }

      return false;
   }
}

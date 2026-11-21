package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.util.Functions;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetFirstPosCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args_) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         Location location = player.getLocation();
         Functions.SetFirstPos(player, location);
      }

      return false;
   }
}

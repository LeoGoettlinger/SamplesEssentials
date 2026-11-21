package de.mistrx.buildpaste.commands;

import de.mistrx.buildpaste.firebase.Firebase;
import de.mistrx.buildpaste.items.PastePaperItem;
import de.mistrx.buildpaste.util.Functions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PastePaperCommand implements CommandExecutor {
   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (sender instanceof Player) {
         Player player = (Player)sender;
         String name = String.join(" ", args);
         String selectedBuild = Firebase.getSelectedBuildingID(player.getUniqueId().toString());
         if (selectedBuild.equals("error")) {
            Functions.sendNotConnectedMessage(player);
            return false;
         }

         player.getInventory().addItem(new ItemStack[]{PastePaperItem.GetPastePaper(name, selectedBuild)});
      }

      return true;
   }
}

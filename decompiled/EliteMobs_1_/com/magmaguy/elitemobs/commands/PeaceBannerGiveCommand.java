package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.peacebanner.PeaceBannerItem;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PeaceBannerGiveCommand extends AdvancedCommand {
   public PeaceBannerGiveCommand() {
      super(List.of("peacebanner"));
      this.addLiteral("give");
      this.addArgument("playerName", new PlayerCommandArgument());
      this.setUsage("/em peacebanner give <player>");
      this.setPermission("elitemobs.peacebanner.admin");
      this.setDescription("Gives a Peace Banner to the specified player.");
   }

   @Override
   public void execute(CommandData commandData) {
      Player player = Bukkit.getPlayer(commandData.getStringArgument("playerName"));
      if (player == null) {
         commandData.getCommandSender().sendMessage("Player not found.");
      } else {
         for (ItemStack overflow : player.getInventory().addItem(new ItemStack[]{PeaceBannerItem.createPeaceBanner()}).values()) {
            player.getWorld().dropItem(player.getLocation(), overflow);
         }

         commandData.getCommandSender().sendMessage("Gave a Peace Banner to " + player.getName() + ".");
      }
   }
}

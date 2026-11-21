package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.menus.ProceduralShopMenu;
import java.util.List;
import org.bukkit.Bukkit;

public class ShopProceduralOtherCommand extends AdvancedCommand {
   public ShopProceduralOtherCommand() {
      super(List.of("shop"));
      this.addLiteral("procedural");
      this.addArgument("player", new PlayerCommandArgument());
      this.setUsage("/em shop procedural <player>");
      this.setPermission("elitemobs.*");
      this.setDescription("Opens the EliteMobs shop for procedurally generated items.");
   }

   @Override
   public void execute(CommandData commandData) {
      try {
         ProceduralShopMenu.shopConstructor(Bukkit.getPlayer(commandData.getStringArgument("player")));
      } catch (Exception var3) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getShopPlayerNotFoundMessage());
      }
   }
}

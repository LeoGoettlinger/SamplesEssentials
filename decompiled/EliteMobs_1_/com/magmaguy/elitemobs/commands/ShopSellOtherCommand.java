package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.menus.SellMenu;
import java.util.List;
import org.bukkit.Bukkit;

public class ShopSellOtherCommand extends AdvancedCommand {
   public ShopSellOtherCommand() {
      super(List.of("shop"));
      this.addLiteral("sell");
      this.addArgument("player", new PlayerCommandArgument());
      this.setUsage("/em shop sell <player>");
      this.setPermission("elitemobs.shop.sell.other");
      this.setDescription("Opens the EliteMobs shop sell menu for the specified player.");
   }

   @Override
   public void execute(CommandData commandData) {
      SellMenu sellMenu = new SellMenu();

      try {
         sellMenu.constructSellMenu(Bukkit.getPlayer(commandData.getStringArgument("player")));
      } catch (Exception var4) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getShopPlayerNotFoundMessage());
      }
   }
}

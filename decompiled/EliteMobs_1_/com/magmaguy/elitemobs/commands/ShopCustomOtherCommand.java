package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.menus.CustomShopMenu;
import java.util.List;
import org.bukkit.Bukkit;

public class ShopCustomOtherCommand extends AdvancedCommand {
   public ShopCustomOtherCommand() {
      super(List.of("shop"));
      this.addLiteral("custom");
      this.addArgument("player", new PlayerCommandArgument());
      this.setUsage("/em shop custom <player>");
      this.setPermission("elitemobs.shop.custom.other");
      this.setDescription("Opens the EliteMobs shop for custom items for the specified player.");
   }

   @Override
   public void execute(CommandData commandData) {
      try {
         CustomShopMenu.customShopConstructor(Bukkit.getPlayer(commandData.getStringArgument("player")));
      } catch (Exception var3) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getShopPlayerNotFoundMessage());
      }
   }
}

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.menus.CustomShopMenu;
import java.util.List;

public class ShopCustomCommand extends AdvancedCommand {
   public ShopCustomCommand() {
      super(List.of("shop"));
      this.addLiteral("custom");
      this.setUsage("/em shop custom");
      this.setPermission("elitemobs.shop.command");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Opens the procedurally generated item shop or teleports the player to the Adventurer's Guild Hub.");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
         CustomShopMenu.customShopInitializer(commandData.getPlayerSender());
      }
   }
}

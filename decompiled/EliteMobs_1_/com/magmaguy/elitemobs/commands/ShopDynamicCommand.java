package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.menus.ProceduralShopMenu;
import java.util.List;

public class ShopDynamicCommand extends AdvancedCommand {
   public ShopDynamicCommand() {
      super(List.of("shop"));
      this.addLiteral("dynamic");
      this.setUsage("/em shop dynamic");
      this.setPermission("elitemobs.shop.dynamic");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Opens the procedurally generated item shop or teleports the player to the Adventurer's Guild Hub.");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
         ProceduralShopMenu.shopInitializer(commandData.getPlayerSender());
      }
   }
}

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.menus.GetLootMenu;
import java.util.List;

public class LootMenuCommand extends AdvancedCommand {
   public LootMenuCommand() {
      super(List.of("loot"));
      this.addLiteral("menu");
      this.setUsage("/em loot menu");
      this.setPermission("elitemobs.loot.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Opens the loot menu.");
   }

   @Override
   public void execute(CommandData commandData) {
      new GetLootMenu(commandData.getPlayerSender());
   }
}

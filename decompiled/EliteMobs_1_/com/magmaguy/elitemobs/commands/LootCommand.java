package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.menus.LootMenu;
import java.util.List;

public class LootCommand extends AdvancedCommand {
   public LootCommand() {
      super(List.of("loot"));
      this.setUsage("/em loot");
      this.setDescription("Open the loot menu for group loot.");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      LootMenu.openMenu(commandData.getPlayerSender());
   }
}

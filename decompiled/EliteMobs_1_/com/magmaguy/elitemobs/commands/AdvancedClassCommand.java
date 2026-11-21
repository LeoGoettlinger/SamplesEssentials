package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.menu.ClassSelectionMenu;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;

public final class AdvancedClassCommand extends AdvancedCommand {
   public AdvancedClassCommand() {
      super(List.of("class"));
      this.setUsage("/em class");
      this.setDescription("Shows your [Alpha] Advanced Combat System class profile.");
      this.setPermission("elitemobs.command");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      ClassSelectionMenu.open(commandData.getPlayerSender());
   }
}

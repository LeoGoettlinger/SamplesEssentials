package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.setup.EliteSetupMenu;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;

public class SetupCommand extends AdvancedCommand {
   public SetupCommand() {
      super(List.of("setup"));
      this.setPermission("elitemobs.setup");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("The main command for setting up EliteMobs!");
      this.setUsage("/em setup");
   }

   @Override
   public void execute(CommandData commandData) {
      EliteSetupMenu.createMenu(commandData.getPlayerSender());
   }
}

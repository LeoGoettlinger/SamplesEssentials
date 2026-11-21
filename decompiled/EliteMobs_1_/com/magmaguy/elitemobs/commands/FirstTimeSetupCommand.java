package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.setup.EliteFirstTimeSetupMenu;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;

public class FirstTimeSetupCommand extends AdvancedCommand {
   public FirstTimeSetupCommand() {
      super(List.of("initialize"));
      this.setUsage("/em initialize");
      this.setPermission("elitemobs.initialize");
      this.setDescription("Does the first time setup of the plugin.");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      EliteFirstTimeSetupMenu.createMenu(commandData.getPlayerSender());
   }
}

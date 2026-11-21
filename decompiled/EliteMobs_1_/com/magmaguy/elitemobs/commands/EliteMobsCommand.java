package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import java.util.ArrayList;

public class EliteMobsCommand extends AdvancedCommand {
   public EliteMobsCommand() {
      super(new ArrayList<>());
      this.setDescription("The main command for EliteMobs, opens the main menu.");
      this.setUsage("/em");
      this.setPermission("elitemobs.command");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      if (DefaultConfig.isEmLeadsToStatusMenu()) {
         new PlayerStatusScreen(commandData.getPlayerSender());
      }
   }
}

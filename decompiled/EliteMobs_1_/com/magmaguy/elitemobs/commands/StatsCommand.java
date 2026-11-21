package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;

public class StatsCommand extends AdvancedCommand {
   public StatsCommand() {
      super(List.of("stats"));
      this.setUsage("/em stats");
      this.setPermission("elitemobs.stats");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Displays EliteMobs server stats.");
   }

   @Override
   public void execute(CommandData commandData) {
      com.magmaguy.elitemobs.commands.admin.StatsCommand.statsHandler(commandData.getCommandSender());
   }
}

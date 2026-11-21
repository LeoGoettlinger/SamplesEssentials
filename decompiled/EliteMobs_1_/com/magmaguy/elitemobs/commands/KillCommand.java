package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import java.util.List;

public class KillCommand extends AdvancedCommand {
   public KillCommand() {
      super(List.of("kill"));
      this.setUsage("/em kill");
      this.setPermission("elitemobs.kill.command");
      this.setDescription("Kills all elites.");
   }

   @Override
   public void execute(CommandData commandData) {
      KillHandler.killAggressiveMobs(commandData.getCommandSender());
   }
}

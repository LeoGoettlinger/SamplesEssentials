package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import java.util.List;

public class KillRadiusCommand extends AdvancedCommand {
   public KillRadiusCommand() {
      super(List.of("kill"));
      this.addArgument("radius", new IntegerCommandArgument("<radius>"));
      this.setUsage("/em kill <radius>");
      this.setPermission("elitemobs.kill.command");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Kills all elites in the specified radius.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedRadius = commandData.getIntegerArgument("radius");
      if (parsedRadius != null) {
         KillHandler.radiusKillAggressiveMobs(commandData.getPlayerSender(), parsedRadius);
      }
   }
}

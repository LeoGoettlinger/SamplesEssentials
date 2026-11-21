package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.KillHandler;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import java.util.List;

public class KillTypeRadiusCommand extends AdvancedCommand {
   public KillTypeRadiusCommand() {
      super(List.of("kill"));
      this.addLiteral("type");
      this.addArgument("type", new EliteEntityTypeCommandArgument());
      this.addArgument("range", new IntegerCommandArgument("<range>"));
      this.setUsage("/em kill type <entityType> <radius>");
      this.setPermission("elitemobs.kill.command");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Kills all elites of the specified type within the specified radius.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedRange = commandData.getIntegerArgument("range");
      if (parsedRange != null) {
         KillHandler.radiusKillSpecificMobs(
            commandData.getPlayerSender(), EliteEntityTypeCommandArgument.parse(commandData.getStringArgument("type")), parsedRange
         );
      }
   }
}

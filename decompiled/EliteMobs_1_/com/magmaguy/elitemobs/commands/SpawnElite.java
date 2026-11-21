package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import java.util.List;
import java.util.Optional;

public class SpawnElite extends AdvancedCommand {
   public SpawnElite() {
      super(List.of("spawn"));
      this.addLiteral("elite");
      this.addArgument("entityType", new EliteEntityTypeCommandArgument());
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.setUsage("/em spawn elite <entityType> <level>");
      this.setPermission("elitemobs.place.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Spawns an elite of the specified type and level.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedLevel != null) {
         SpawnCommand.spawnEliteEntityTypeCommand(
            commandData.getPlayerSender(), EliteEntityTypeCommandArgument.parse(commandData.getStringArgument("entityType")), parsedLevel, Optional.empty()
         );
      }
   }
}

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import java.util.List;

public class SpawnBossLevelCommand extends AdvancedCommand {
   public SpawnBossLevelCommand() {
      super(List.of("spawn"));
      this.addLiteral("boss");
      this.addArgument("filename", new DynamicListStringCommandArgument(() -> CustomBossesConfig.getCustomBosses().keySet().stream().toList(), "<filename>"));
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.setUsage("/em spawn boss <filename> <level>");
      this.setPermission("elitemobs.place.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Spawns a custom boss at the location the user is looking at with the specified level.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedLevel != null) {
         SpawnCommand.spawnCustomBossCommand(commandData.getPlayerSender(), commandData.getStringArgument("filename"), parsedLevel);
      }
   }
}

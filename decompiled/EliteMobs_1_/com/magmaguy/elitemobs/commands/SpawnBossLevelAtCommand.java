package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.DoubleCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.WorldCommandArgument;
import java.util.List;
import org.bukkit.util.Vector;

public class SpawnBossLevelAtCommand extends AdvancedCommand {
   public SpawnBossLevelAtCommand() {
      super(List.of("spawn"));
      this.addLiteral("bossAt");
      this.addArgument("filename", new DynamicListStringCommandArgument(() -> CustomBossesConfig.getCustomBosses().keySet().stream().toList(), "<filename>"));
      this.addArgument("worldName", new WorldCommandArgument("<worldName>"));
      this.addArgument("x", new DoubleCommandArgument("<x>"));
      this.addArgument("y", new DoubleCommandArgument("<y>"));
      this.addArgument("z", new DoubleCommandArgument("<z>"));
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.setUsage("/em spawn bossAt <filename> <worldName> <x> <y> <z> <level>");
      this.setPermission("elitemobs.place.admin");
      this.setDescription("Spawns a custom boss at the specified location with the specified level.");
   }

   @Override
   public void execute(CommandData commandData) {
      Double parsedX = commandData.getDoubleArgument("x");
      Double parsedY = commandData.getDoubleArgument("y");
      Double parsedZ = commandData.getDoubleArgument("z");
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedX != null && parsedY != null && parsedZ != null && parsedLevel != null) {
         SpawnCommand.spawnCustomBossCommand(
            commandData.getCommandSender(),
            commandData.getStringArgument("filename"),
            commandData.getStringArgument("worldName"),
            new Vector(parsedX, parsedY, parsedZ),
            parsedLevel
         );
      }
   }
}

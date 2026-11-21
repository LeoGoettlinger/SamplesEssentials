package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import java.util.List;

public class SpawnBossCommand extends AdvancedCommand {
   public SpawnBossCommand() {
      super(List.of("spawn"));
      this.addLiteral("boss");
      this.addArgument("filename", new DynamicListStringCommandArgument(() -> CustomBossesConfig.getCustomBosses().keySet().stream().toList(), "<filename>"));
      this.setUsage("/em spawn boss <filename>");
      this.setPermission("elitemobs.place.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Spawns a custom boss at the location the user is looking at.");
   }

   @Override
   public void execute(CommandData commandData) {
      SpawnCommand.spawnCustomBossCommand(commandData.getPlayerSender(), commandData.getStringArgument("filename"));
   }
}

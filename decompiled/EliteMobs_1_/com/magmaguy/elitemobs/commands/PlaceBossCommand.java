package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class PlaceBossCommand extends AdvancedCommand {
   public PlaceBossCommand() {
      super(List.of("place"));
      this.addLiteral("boss");
      this.addArgument("filename", new DynamicListStringCommandArgument(() -> {
         List<String> regionalBosses = new ArrayList<>();

         for (Entry<String, ? extends CustomBossesConfigFields> entry : CustomBossesConfig.getCustomBosses().entrySet()) {
            if (entry.getValue().isRegionalBoss()) {
               regionalBosses.add(entry.getKey());
            }
         }

         return regionalBosses;
      }, "<filename>"));
      this.setUsage("/em place boss <filename>");
      this.setPermission("elitemobs.place.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Add a spawn location for the specified boss at your current location.");
   }

   @Override
   public void execute(CommandData commandData) {
      CustomBossCommandHandler.addSpawnLocation(commandData.getStringArgument("filename"), commandData.getPlayerSender());
   }
}

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import java.util.List;
import java.util.UUID;

public class TrackBossCommand extends AdvancedCommand {
   public TrackBossCommand() {
      super(List.of("track"));
      this.addLiteral("boss");
      this.addArgument("id", new ListStringCommandArgument("<id>"));
      this.setDescription("Tracks a Custom Boss.");
      this.setUsage("/em track boss <id>");
      this.setSenderType(SenderType.PLAYER);
      this.setPermission("elitemobs.boss.track");
   }

   @Override
   public void execute(CommandData commandData) {
      try {
         for (CustomBossEntity customBossEntity : CustomBossEntity.getTrackableCustomBosses()) {
            if (customBossEntity.getEliteUUID().equals(UUID.fromString(commandData.getStringArgument("id")))) {
               customBossEntity.getBossTrackingBar().addTrackingPlayer(commandData.getPlayerSender());
               return;
            }
         }

         Logger.sendSimpleMessage(commandData.getCommandSender(), DefaultConfig.getBossAlreadyGoneMessage());
      } catch (Exception var4) {
         Logger.sendSimpleMessage(commandData.getCommandSender(), DefaultConfig.getBossAlreadyGoneMessage());
      }
   }
}

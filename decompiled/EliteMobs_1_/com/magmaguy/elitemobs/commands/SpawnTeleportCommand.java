package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.api.PlayerPreTeleportEvent;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;

public class SpawnTeleportCommand extends AdvancedCommand {
   public SpawnTeleportCommand() {
      super(List.of("spawntp"));
      this.setPermission("elitemobs.teleport.spawn");
      this.setUsage("/em spawntp");
      this.setDescription("Teleports players to the server spawn.");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      if (DefaultConfig.getDefaultSpawnLocation() != null) {
         PlayerPreTeleportEvent.teleportPlayer(commandData.getPlayerSender(), DefaultConfig.getDefaultSpawnLocation());
      }
   }
}

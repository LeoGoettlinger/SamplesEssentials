package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import java.util.List;

public class RespawnAllCommand extends AdvancedCommand {
   public RespawnAllCommand() {
      super(List.of("respawn"));
      this.addLiteral("all");
      this.setUsage("/em respawn all");
      this.setPermission("elitemobs.respawn.force");
      this.setDescription("Forces all regional bosses to respawn.");
   }

   @Override
   public void execute(CommandData commandData) {
      RegionalBossEntity.getRegionalBossEntities().forEach(regionalBossEntity -> {
         if (regionalBossEntity.isRespawning()) {
            regionalBossEntity.forceRespawn();
         }
      });
   }
}

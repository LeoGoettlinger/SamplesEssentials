package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import java.util.List;

public class DismissCommand extends AdvancedCommand {
   public DismissCommand() {
      super(List.of("dismiss"));
      this.setDescription("Dismisses /em menu message.");
      this.setUsage("/em dismiss");
      this.setPermission("elitemobs.dismiss");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PlayerData.setDismissEMStatusScreenMessage(
         commandData.getPlayerSender(), !PlayerData.getDismissEMStatusScreenMessage(commandData.getPlayerSender().getUniqueId())
      );
   }
}

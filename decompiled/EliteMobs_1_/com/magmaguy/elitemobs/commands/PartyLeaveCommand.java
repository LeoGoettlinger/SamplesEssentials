package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.parties.PartyManager;
import java.util.List;

public class PartyLeaveCommand extends AdvancedCommand {
   public PartyLeaveCommand() {
      super(List.of("party"));
      this.addLiteral("leave");
      this.setUsage("/em party leave");
      this.setDescription(PartyConfig.getCommandLeaveDescription());
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PartyManager.leave(commandData.getPlayerSender());
   }
}

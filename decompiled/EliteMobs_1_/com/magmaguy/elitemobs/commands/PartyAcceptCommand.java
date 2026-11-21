package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.parties.PartyManager;
import java.util.List;

public class PartyAcceptCommand extends AdvancedCommand {
   public PartyAcceptCommand() {
      super(List.of("party"));
      this.addLiteral("accept");
      this.setUsage("/em party accept");
      this.setDescription(PartyConfig.getCommandAcceptDescription());
      this.setPermission("elitemobs.party");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PartyManager.accept(commandData.getPlayerSender());
   }
}

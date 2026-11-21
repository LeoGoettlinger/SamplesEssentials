package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.parties.PartyManager;
import java.util.List;

public class PartyCreateCommand extends AdvancedCommand {
   public PartyCreateCommand() {
      super(List.of("party"));
      this.addLiteral("create");
      this.setUsage("/em party create");
      this.setDescription(PartyConfig.getCommandCreateDescription());
      this.setPermission("elitemobs.party");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PartyManager.create(commandData.getPlayerSender());
   }
}

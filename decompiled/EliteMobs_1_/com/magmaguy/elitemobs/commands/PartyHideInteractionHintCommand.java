package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.parties.PartyInteractionHint;
import java.util.List;

public class PartyHideInteractionHintCommand extends AdvancedCommand {
   public PartyHideInteractionHintCommand() {
      super(List.of("party"));
      this.addLiteral("hideinteractionhint");
      this.setUsage("/em party hideinteractionhint");
      this.setDescription(PartyConfig.getCommandHideInteractionHintDescription());
      this.setPermission("elitemobs.party");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PartyInteractionHint.disable(commandData.getPlayerSender());
   }
}

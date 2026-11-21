package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.parties.PartyDungeonReadyCheckManager;
import java.util.List;

public class PartyDeclineCommand extends AdvancedCommand {
   public PartyDeclineCommand() {
      super(List.of("party"));
      this.addLiteral("decline");
      this.addArgument("token", new ListStringCommandArgument(PartyConfig.getReadyCheckTokenHint()));
      this.setUsage("/em party decline <token>");
      this.setDescription(PartyConfig.getCommandDeclineDescription());
      this.setPermission("elitemobs.party");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PartyDungeonReadyCheckManager.decline(commandData.getPlayerSender(), commandData.getStringArgument("token"));
   }
}

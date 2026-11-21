package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.parties.PartyDungeonReadyCheckManager;
import java.util.List;

public class PartyReadyCommand extends AdvancedCommand {
   public PartyReadyCommand() {
      super(List.of("party"));
      this.addLiteral("ready");
      this.addArgument("token", new ListStringCommandArgument(PartyConfig.getReadyCheckTokenHint()));
      this.setUsage("/em party ready <token>");
      this.setDescription(PartyConfig.getCommandReadyDescription());
      this.setPermission("elitemobs.party");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PartyDungeonReadyCheckManager.ready(commandData.getPlayerSender(), commandData.getStringArgument("token"));
   }
}

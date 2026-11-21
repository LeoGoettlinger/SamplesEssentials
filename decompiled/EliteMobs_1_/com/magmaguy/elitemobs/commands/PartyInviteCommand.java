package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import com.magmaguy.elitemobs.parties.PartyManager;
import java.util.List;

public class PartyInviteCommand extends AdvancedCommand {
   public PartyInviteCommand() {
      super(List.of("party"));
      this.addLiteral("invite");
      this.addArgument("player", new PlayerCommandArgument());
      this.setUsage("/em party invite <player>");
      this.setDescription(PartyConfig.getCommandInviteDescription());
      this.setPermission("elitemobs.party");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PartyManager.invite(commandData.getPlayerSender(), commandData.getStringArgument("player"));
   }
}

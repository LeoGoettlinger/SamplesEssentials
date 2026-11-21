package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.parties.PartyInventoryMenu;
import java.util.List;

public class PartyMenuCommand extends AdvancedCommand {
   public PartyMenuCommand() {
      super(List.of("party"));
      this.addLiteral("menu");
      this.setUsage("/em party menu");
      this.setDescription(PartyConfig.getCommandMenuDescription());
      this.setPermission("elitemobs.party");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      PartyInventoryMenu.open(commandData.getPlayerSender());
   }
}

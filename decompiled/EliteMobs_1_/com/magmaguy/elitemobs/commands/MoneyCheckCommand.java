package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import java.util.List;

public class MoneyCheckCommand extends AdvancedCommand {
   public MoneyCheckCommand() {
      super(List.of("money"));
      this.addLiteral("check");
      this.setUsage("/em money check");
      this.setPermission("elitemobs.money.check.self");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Checks your EliteMobs currency.");
   }

   @Override
   public void execute(CommandData commandData) {
      if (DefaultConfig.isOtherCommandsLeadToEMStatusMenu()) {
         new PlayerStatusScreen(commandData.getPlayerSender());
      } else {
         CurrencyCommandsHandler.walletCommand(commandData.getPlayerSender());
      }
   }
}

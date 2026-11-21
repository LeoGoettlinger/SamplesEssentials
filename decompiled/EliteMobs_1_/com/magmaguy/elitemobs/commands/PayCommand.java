package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DoubleCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class PayCommand extends AdvancedCommand {
   public PayCommand() {
      super(List.of("pay"));
      this.addArgument("player", new PlayerCommandArgument());
      this.addArgument("amount", new DoubleCommandArgument("<amount>"));
      this.setDescription("Send money to a player, minus tax.");
      this.setUsage("/em pay <player> <amount>");
      this.setPermission("elitemobs.money.pay");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      Double parsedAmount = commandData.getDoubleArgument("amount");
      if (parsedAmount != null) {
         CurrencyCommandsHandler.payCommand(commandData.getPlayerSender(), commandData.getStringArgument("player"), parsedAmount);
      }
   }
}

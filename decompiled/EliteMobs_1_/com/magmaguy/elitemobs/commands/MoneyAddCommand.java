package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class MoneyAddCommand extends AdvancedCommand {
   public MoneyAddCommand() {
      super(List.of("money"));
      this.addLiteral("add");
      this.addArgument("player", new PlayerCommandArgument());
      this.addArgument("amount", new IntegerCommandArgument("<amount>"));
      this.setUsage("/em money add <player> <amount>");
      this.setPermission("elitemobs.money.admin");
      this.setDescription("Gives the specified amount of money to the designated player.");
   }

   @Override
   public void execute(CommandData commandData) {
      Double parsedAmount = commandData.getDoubleArgument("amount");
      if (parsedAmount != null) {
         CurrencyCommandsHandler.addCommand(commandData.getCommandSender(), commandData.getStringArgument("player"), parsedAmount);
      }
   }
}

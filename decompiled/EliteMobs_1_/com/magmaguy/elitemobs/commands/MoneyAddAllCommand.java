package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import java.util.List;

public class MoneyAddAllCommand extends AdvancedCommand {
   public MoneyAddAllCommand() {
      super(List.of("money"));
      this.addLiteral("addAll");
      this.addArgument("amount", new IntegerCommandArgument("<amount>"));
      this.setUsage("/em money addAll <amount>");
      this.setPermission("elitemobs.money.admin");
      this.setDescription("Gives every player on the server the specified amount of money.");
   }

   @Override
   public void execute(CommandData commandData) {
      Double parsedAmount = commandData.getDoubleArgument("amount");
      if (parsedAmount != null) {
         CurrencyCommandsHandler.addAllCommand(commandData.getCommandSender(), parsedAmount);
      }
   }
}

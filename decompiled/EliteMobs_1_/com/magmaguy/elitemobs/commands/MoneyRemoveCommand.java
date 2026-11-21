package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.DoubleCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class MoneyRemoveCommand extends AdvancedCommand {
   public MoneyRemoveCommand() {
      super(List.of("money"));
      this.addLiteral("remove");
      this.addArgument("player", new PlayerCommandArgument());
      this.addArgument("amount", new DoubleCommandArgument("<amount>"));
      this.setUsage("/em money remove <player> <amount>");
      this.setPermission("elitemobs.money.admin");
      this.setDescription("Deducts the specified amount of currency from a player.");
   }

   @Override
   public void execute(CommandData commandData) {
      Double parsedAmount = commandData.getDoubleArgument("amount");
      if (parsedAmount != null) {
         CurrencyCommandsHandler.subtractCommand(commandData.getStringArgument("player"), parsedAmount);
      }
   }
}

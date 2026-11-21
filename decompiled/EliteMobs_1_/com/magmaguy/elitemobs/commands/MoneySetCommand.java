package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.DoubleCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class MoneySetCommand extends AdvancedCommand {
   public MoneySetCommand() {
      super(List.of("money"));
      this.addLiteral("set");
      this.addArgument("player", new PlayerCommandArgument());
      this.addArgument("amount", new DoubleCommandArgument("<amount>"));
      this.setUsage("/em money set <player> <amount>");
      this.setPermission("elitemobs.money.admin");
      this.setDescription("Sets the specified amount of currency for the specified player.");
   }

   @Override
   public void execute(CommandData commandData) {
      Double parsedAmount = commandData.getDoubleArgument("amount");
      if (parsedAmount != null) {
         CurrencyCommandsHandler.setCommand(commandData.getCommandSender(), commandData.getStringArgument("player"), parsedAmount);
      }
   }
}

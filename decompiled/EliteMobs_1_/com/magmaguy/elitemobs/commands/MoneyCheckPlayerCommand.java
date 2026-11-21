package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class MoneyCheckPlayerCommand extends AdvancedCommand {
   public MoneyCheckPlayerCommand() {
      super(List.of("money"));
      this.addLiteral("check");
      this.addArgument("player", new PlayerCommandArgument());
      this.setUsage("/em money check <player>");
      this.setPermission("elitemobs.money.check.others");
      this.setDescription("Checks the currency of the specified player.");
   }

   @Override
   public void execute(CommandData commandData) {
      CurrencyCommandsHandler.checkCommand(commandData.getCommandSender(), commandData.getStringArgument("player"));
   }
}

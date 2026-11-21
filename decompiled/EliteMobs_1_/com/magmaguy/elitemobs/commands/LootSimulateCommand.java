package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.SimLootCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class LootSimulateCommand extends AdvancedCommand {
   public LootSimulateCommand() {
      super(List.of("loot"));
      this.addLiteral("simulate");
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.addArgument("playerName", new PlayerCommandArgument());
      this.setUsage("/em loot simulate <level> <playerName>");
      this.setPermission("elitemobs.loot.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Simulates loot drops for the specified level and player.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedLevel != null) {
         SimLootCommand.run(commandData.getPlayerSender(), parsedLevel, commandData.getStringArgument("playerName"));
      }
   }
}

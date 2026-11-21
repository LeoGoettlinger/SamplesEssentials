package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.SimLootCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class LootSimulateMultipleCommand extends AdvancedCommand {
   public LootSimulateMultipleCommand() {
      super(List.of("loot"));
      this.addLiteral("simulate");
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.addArgument("times", new IntegerCommandArgument("<times>"));
      this.addArgument("playerName", new PlayerCommandArgument());
      this.setUsage("/em loot simulate <level> <times> <playerName>");
      this.setPermission("elitemobs.loot.admin");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Simulates loot drops for the specified amount of times for the specified level and player.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      Integer parsedTimes = commandData.getIntegerArgument("times");
      if (parsedLevel != null && parsedTimes != null) {
         SimLootCommand.runMultipleTimes(commandData.getPlayerSender(), parsedLevel, parsedTimes, commandData.getStringArgument("playerName"));
      }
   }
}

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.admin.SimLootCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class LootRandomCommand extends AdvancedCommand {
   public LootRandomCommand() {
      super(List.of("loot"));
      this.addLiteral("random");
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.addArgument("playerName", new PlayerCommandArgument());
      this.setUsage("/em loot random <level> <player>");
      this.setPermission("elitemobs.loot.admin");
      this.setDescription("Gives the player random loot of the specified level.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedLevel != null) {
         SimLootCommand.forcePositiveLoot(commandData.getCommandSender(), commandData.getStringArgument("playerName"), parsedLevel);
      }
   }
}

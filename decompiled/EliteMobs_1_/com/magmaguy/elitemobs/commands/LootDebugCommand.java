package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.commands.admin.GetTierCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import java.util.List;

public class LootDebugCommand extends AdvancedCommand {
   public LootDebugCommand() {
      super(List.of("loot"));
      this.addLiteral("debug");
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.setUsage("/em loot debug <level>");
      this.setPermission("elitemobs.loot.debug");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Equips players with a complete armor set, weapons set of the specified level, and food for testing purposes.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedLevel != null) {
         int level = parsedLevel;
         GetTierCommand.getUnbreakable(commandData.getPlayerSender(), level);
         AdvancedCombatModule.scaleClassesForDebugLoadout(commandData.getPlayerSender(), level);
      }
   }
}

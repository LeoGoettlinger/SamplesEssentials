package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatModule;
import com.magmaguy.elitemobs.commands.admin.GetTierCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.IntegerCommandArgument;
import java.util.List;

public class LootDebugLimitedCommand extends AdvancedCommand {
   public LootDebugLimitedCommand() {
      super(List.of("loot"));
      this.addLiteral("debug");
      this.addArgument("level", new IntegerCommandArgument("<level>"));
      this.addLiteral("limited");
      this.setUsage("/em loot debug <level> limited");
      this.setPermission("elitemobs.loot.debug");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Equips players with a limited debug loadout of the specified level for faster testing.");
   }

   @Override
   public void execute(CommandData commandData) {
      Integer parsedLevel = commandData.getIntegerArgument("level");
      if (parsedLevel != null) {
         GetTierCommand.getLimited(commandData.getPlayerSender(), parsedLevel);
         AdvancedCombatModule.scaleClassesForDebugLoadout(commandData.getPlayerSender(), parsedLevel);
      }
   }
}

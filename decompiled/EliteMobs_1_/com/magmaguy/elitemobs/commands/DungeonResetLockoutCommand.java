package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class DungeonResetLockoutCommand extends AdvancedCommand {
   public DungeonResetLockoutCommand() {
      super(List.of("dungeon"));
      this.addLiteral("resetlockout");
      this.addArgument("player", new PlayerCommandArgument());
      this.setUsage("/em dungeon resetlockout <player>");
      this.setPermission("elitemobs.dungeon.resetlockout");
      this.setDescription("Clears all dungeon boss lockouts for a specific player.");
   }

   @Override
   public void execute(CommandData commandData) {
      DungeonCommands.resetLockout(commandData.getCommandSender(), commandData.getStringArgument("player"));
   }
}

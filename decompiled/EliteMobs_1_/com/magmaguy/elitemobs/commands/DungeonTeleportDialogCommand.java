package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

public class DungeonTeleportDialogCommand extends AdvancedCommand {
   public DungeonTeleportDialogCommand() {
      super(List.of("dungeontpdialog"));
      this.addArgument("dungeonID", new ListStringCommandArgument("Dungeon ID (dialog navigation only)"));
      this.setPermission("elitemobs.dungeon.tp");
      this.setDescription("Internal dialog teleport navigation command.");
      this.setUsage("/em dungeontpdialog <dungeonID>");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      DungeonCommands.teleport(commandData.getPlayerSender(), commandData.getStringArgument("dungeonID"), DungeonCommands.TeleportMenuSource.DIALOGUE);
   }
}

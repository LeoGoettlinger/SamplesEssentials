package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

public class DungeonTeleportCommand extends AdvancedCommand {
   public DungeonTeleportCommand() {
      super(List.of("dungeontp"));
      this.addArgument("dungeonID", new ListStringCommandArgument("Dungeon ID (you can't run this manually!)"));
      this.setPermission("elitemobs.dungeon.tp");
      this.setDescription("Teleports players to Lairs, Minidungeons and Dungeons.");
      this.setUsage("/em dungeontp <dungeonID>");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      DungeonCommands.teleport(commandData.getPlayerSender(), commandData.getStringArgument("dungeonID"));
   }
}

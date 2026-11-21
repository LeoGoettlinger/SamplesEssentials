package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.menus.ArenaMenu;
import java.util.List;

public class ArenaCommand extends AdvancedCommand {
   public ArenaCommand() {
      super(List.of("arena"));
      this.addArgument("arenaID", new ListStringCommandArgument("Arena ID (you can't run this manually!)"));
      this.setUsage("/em arena <arenaID>");
      this.setPermission("elitemobs.arena.start");
      this.setDescription("Open the Arena menu.");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      new ArenaMenu().constructArenaMenu(commandData.getPlayerSender(), commandData.getStringArgument("arenaID"));
   }
}

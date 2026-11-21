package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.menus.RepairMenu;
import java.util.List;

public class RepairCommand extends AdvancedCommand {
   public RepairCommand() {
      super(List.of("repair"));
      this.setUsage("/em repair");
      this.setPermission("elitemobs.repair.command");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Opens the repair item menu or teleports the player to the Adventurer's Guild Hub.");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
         RepairMenu repairMenu = new RepairMenu();
         repairMenu.constructRepairMenu(commandData.getPlayerSender());
      }
   }
}

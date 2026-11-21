package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.menus.UnbindMenu;
import java.util.List;

public class UnbindCommand extends AdvancedCommand {
   public UnbindCommand() {
      super(List.of("unbind"));
      this.setUsage("/em unbind");
      this.setPermission("elitemobs.unbind.command");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Opens the unbind menu or teleports the player to the Adventurer's Guild Hub.");
   }

   @Override
   public void execute(CommandData commandData) {
      if (!com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand.adventurersGuildTeleport(commandData.getPlayerSender())) {
         UnbindMenu unbindMenu = new UnbindMenu();
         unbindMenu.constructUnbinderMenu(commandData.getPlayerSender());
      }
   }
}

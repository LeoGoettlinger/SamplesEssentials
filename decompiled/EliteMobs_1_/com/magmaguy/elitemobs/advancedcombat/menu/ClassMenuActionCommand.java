package com.magmaguy.elitemobs.advancedcombat.menu;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

final class ClassMenuActionCommand extends AdvancedCommand {
   private final ClassMenuCoordinator coordinator;

   ClassMenuActionCommand(ClassMenuCoordinator coordinator) {
      super(List.of("_classmenu"));
      this.coordinator = coordinator;
      this.addArgument("token", new ListStringCommandArgument("<menu-action>"));
      this.setUsage("/em _classmenu <menu-action>");
      this.setDescription("Handles a short-lived [Alpha] Advanced Combat System class-menu action.");
      this.setPermission("elitemobs.command");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      this.coordinator.dispatch(commandData.getPlayerSender(), commandData.getStringArgument("token"));
   }
}

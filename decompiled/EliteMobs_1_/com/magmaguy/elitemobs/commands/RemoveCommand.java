package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;

public class RemoveCommand extends AdvancedCommand {
   public RemoveCommand() {
      super(List.of("remove"));
      this.setUsage("/em remove");
      this.setPermission("elitemobs.remove");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Toggle removal mode for EliteMobs, which can permanently remove any EliteMobs-related content.");
   }

   @Override
   public void execute(CommandData commandData) {
      com.magmaguy.elitemobs.commands.admin.RemoveCommand.remove(commandData.getPlayerSender());
   }
}

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import java.util.List;

public class TransitiveBlocksCancelCommand extends AdvancedCommand {
   public TransitiveBlocksCancelCommand() {
      super(List.of("transitiveBlocks"));
      this.addLiteral("cancel");
      this.setUsage("/em transitiveBlocks cancel");
      this.setDescription("Cancels transitive block registration.");
      this.setSenderType(SenderType.PLAYER);
      this.setPermission("elitemobs.transitiveblocks");
   }

   @Override
   public void execute(CommandData commandData) {
      TransitiveBlockCommand.processCommand(commandData.getPlayerSender());
   }
}

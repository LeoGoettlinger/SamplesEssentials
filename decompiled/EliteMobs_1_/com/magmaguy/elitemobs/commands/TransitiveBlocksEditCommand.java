package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.elitemobs.mobconstructor.custombosses.transitiveblocks.TransitiveBlockCommand;
import java.util.List;

public class TransitiveBlocksEditCommand extends AdvancedCommand {
   public TransitiveBlocksEditCommand() {
      super(List.of("transitiveBlocks"));
      this.addLiteral("edit");
      this.addArgument("filename", new DynamicListStringCommandArgument(() -> CustomBossesConfig.getCustomBosses().keySet().stream().toList(), "<filename>"));
      this.addArgument("type", new ListStringCommandArgument(List.of("ON_SPAWN", "ON_REMOVE"), "<ON_SPAWN>/<ON_REMOVE>"));
      this.setUsage("/em transitiveBlocks edit <filename> <ON_SPAWN/ON_REMOVE>");
      this.setDescription("Edits transitive blocks for use by regional bosses.");
      this.setSenderType(SenderType.PLAYER);
      this.setPermission("elitemobs.transitiveblocks");
   }

   @Override
   public void execute(CommandData commandData) {
      TransitiveBlockCommand.processCommand(
         commandData.getPlayerSender(), commandData.getStringArgument("filename"), commandData.getStringArgument("type"), true
      );
   }
}

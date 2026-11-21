package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;

public class ShareItemCommand extends AdvancedCommand {
   public ShareItemCommand() {
      super(List.of("shareItem"));
      this.setUsage("/em shareItem");
      this.setPermission("elitemobs.shareitem");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Shares the stats of the currently held EliteMobs item in chat.");
   }

   @Override
   public void execute(CommandData commandData) {
      ShareItem.showOnChat(commandData.getPlayerSender());
   }
}

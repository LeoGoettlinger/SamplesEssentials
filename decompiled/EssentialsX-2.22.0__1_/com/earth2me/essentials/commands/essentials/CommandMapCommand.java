package com.earth2me.essentials.commands.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.commands.EssentialsTreeNode;
import java.util.Map.Entry;

public class CommandMapCommand extends EssentialsTreeNode {
   public CommandMapCommand() {
      super("cmd", "commands");
   }

   @Override
   protected void run(CommandSource sender, String commandLabel, String[] args) throws Exception {
      if (this.ess.getAlternativeCommandsHandler().disabledCommands().size() == 0) {
         sender.sendTl("blockListEmpty");
      } else {
         sender.sendTl("blockList");

         for (Entry<String, String> entry : this.ess.getAlternativeCommandsHandler().disabledCommands().entrySet()) {
            sender.sendMessage(entry.getKey() + " => " + entry.getValue());
         }
      }
   }
}

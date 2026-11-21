package com.earth2me.essentials.commands.essentials;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.commands.EssentialsTreeNode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DebugCommand extends EssentialsTreeNode {
   public DebugCommand() {
      super("debug", "verbose");
   }

   @Override
   protected void run(CommandSource sender, String commandLabel, String[] args) throws Exception {
      boolean newDebugState;
      if (args.length > 0) {
         if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("true")) {
            newDebugState = true;
         } else if (!args[0].equalsIgnoreCase("off") && !args[0].equalsIgnoreCase("false")) {
            newDebugState = !this.ess.getSettings().isDebug();
         } else {
            newDebugState = false;
         }
      } else {
         newDebugState = !this.ess.getSettings().isDebug();
      }

      this.ess.getSettings().setDebug(newDebugState);
      sender.sendMessage("Essentials " + this.ess.getDescription().getVersion() + " debug mode " + (this.ess.getSettings().isDebug() ? "enabled" : "disabled"));
   }

   @Override
   protected List<String> tabComplete(CommandSource sender, String commandLabel, String[] args) {
      return args.length == 1 ? Arrays.asList("on", "off") : Collections.emptyList();
   }
}

package com.viaversion.viaversion.commands.defaultsubs;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.command.ViaCommandSender;
import com.viaversion.viaversion.api.command.ViaSubCommand;
import com.viaversion.viaversion.api.configuration.ViaVersionConfig;

public class DontBugMeSubCmd implements ViaSubCommand {
   @Override
   public String name() {
      return "dontbugme";
   }

   @Override
   public String description() {
      return "Toggle checking for updates.";
   }

   @Override
   public boolean execute(ViaCommandSender sender, String[] args) {
      ViaVersionConfig config = Via.getConfig();
      boolean newValue = !config.isCheckForUpdates();
      config.setCheckForUpdates(newValue);
      config.save();
      this.sendMessage(sender, "&6We will %snotify &6you about updates.", new Object[]{newValue ? "&a" : "&cnot "});
      return true;
   }
}

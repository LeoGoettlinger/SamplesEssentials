package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;

public class SetupDoneCommand extends AdvancedCommand {
   public SetupDoneCommand() {
      super(List.of("setup"));
      this.addLiteral("done");
      this.setUsage("/em setup done");
      this.setPermission("elitemobs.setup");
      this.setDescription("Toggles whether the setup message will show up.");
   }

   @Override
   public void execute(CommandData commandData) {
      DefaultConfig.toggleSetupDone();
      if (DefaultConfig.isSetupDone()) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSetupDoneDisableMessage());
      } else {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getSetupDoneEnableMessage());
      }
   }
}

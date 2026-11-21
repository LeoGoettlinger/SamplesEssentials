package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.utils.DebugMessage;
import java.util.List;

public class DebugCommand extends AdvancedCommand {
   public DebugCommand() {
      super(List.of("debug"));
      this.setUsage("/em debug");
      this.setPermission("elitemobs.debug");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Toggles debug mode to show combat damage calculations.");
   }

   @Override
   public void execute(CommandData commandData) {
      boolean enabled = DebugMessage.toggleDebug(commandData.getPlayerSender());
      if (enabled) {
         Logger.sendMessage(commandData.getPlayerSender(), CommandMessagesConfig.getDebugEnabledMessage());
      } else {
         Logger.sendMessage(commandData.getPlayerSender(), CommandMessagesConfig.getDebugDisabledMessage());
      }
   }
}

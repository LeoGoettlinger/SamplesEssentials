package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;

public class QuitCommand extends AdvancedCommand {
   public QuitCommand() {
      super(List.of("quit"));
      this.setDescription("When in instanced content, makes the player leave the instance.");
      this.setUsage("/em quit");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      MatchInstance matchInstance = MatchInstance.getAnyPlayerInstance(commandData.getPlayerSender());
      if (matchInstance != null) {
         matchInstance.removeAnyKind(commandData.getPlayerSender());
      } else {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getNotQueuedForInstanceMessage());
      }
   }
}

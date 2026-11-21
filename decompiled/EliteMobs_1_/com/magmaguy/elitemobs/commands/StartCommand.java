package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.util.List;

public class StartCommand extends AdvancedCommand {
   public StartCommand() {
      super(List.of("start"));
      this.setUsage("/em start");
      this.setDescription("When in instanced content, makes the player start the instance.");
      this.setSenderType(SenderType.PLAYER);
      this.setPermission("elitemobs.instance.start");
   }

   @Override
   public void execute(CommandData commandData) {
      MatchInstance matchInstance = MatchInstance.getPlayerInstance(commandData.getPlayerSender());
      if (matchInstance != null) {
         matchInstance.countdownMatch();
      } else {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getNotQueuedForInstanceMessage());
      }
   }
}

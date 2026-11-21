package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import java.util.List;

public class QuestBypassCommand extends AdvancedCommand {
   public QuestBypassCommand() {
      super(List.of("quest"));
      this.addLiteral("bypass");
      this.setUsage("/em quest bypass");
      this.setPermission("elitemobs.quest.bypass");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Bypasses permission restrictions for elite quests.");
   }

   @Override
   public void execute(CommandData commandData) {
      QuestCommand.bypassQuestRequirements(commandData.getPlayerSender());
      if (PlayerQuestCooldowns.bypassesQuestRestrictions(commandData.getPlayerSender())) {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getQuestBypassOnMessage());
      } else {
         Logger.sendMessage(commandData.getCommandSender(), CommandMessagesConfig.getQuestBypassOffMessage());
      }
   }
}

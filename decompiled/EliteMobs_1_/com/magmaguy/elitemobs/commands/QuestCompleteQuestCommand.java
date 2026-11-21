package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

public class QuestCompleteQuestCommand extends AdvancedCommand {
   public QuestCompleteQuestCommand() {
      super(List.of("quest"));
      this.addLiteral("complete");
      this.addArgument("id", new ListStringCommandArgument("<id>"));
      this.setUsage("/em quest complete <id>");
      this.setPermission("elitemobs.quest.complete");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Completes a quest for a player. Meant to be used via menu, not directly.");
   }

   @Override
   public void execute(CommandData commandData) {
      QuestCommand.completeQuest(commandData.getStringArgument("id"), commandData.getPlayerSender());
   }
}

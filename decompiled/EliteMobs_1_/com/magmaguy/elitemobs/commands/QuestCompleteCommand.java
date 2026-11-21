package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import java.util.List;

public class QuestCompleteCommand extends AdvancedCommand {
   public QuestCompleteCommand() {
      super(List.of("quest"));
      this.addLiteral("complete");
      this.setUsage("/em quest complete");
      this.setPermission("elitemobs.quest.complete.force");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Forces all of your currently active elite quests to be completed. For debugging use only.");
   }

   @Override
   public void execute(CommandData commandData) {
      QuestCommand.completeQuest(commandData.getPlayerSender());
   }
}

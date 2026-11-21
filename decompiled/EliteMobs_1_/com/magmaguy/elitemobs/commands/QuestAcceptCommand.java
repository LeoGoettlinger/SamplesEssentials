package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

public class QuestAcceptCommand extends AdvancedCommand {
   public QuestAcceptCommand() {
      super(List.of("quest"));
      this.addLiteral("accept");
      this.addArgument("questID", new ListStringCommandArgument("<questID>"));
      this.setUsage("/em quest accept <questID>");
      this.setPermission("elitemobs.quest.accept");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Accepts a quest. Used via menu, can't be directly used.");
   }

   @Override
   public void execute(CommandData commandData) {
      QuestCommand.joinQuest(commandData.getStringArgument("questID"), commandData.getPlayerSender());
   }
}

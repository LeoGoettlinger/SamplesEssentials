package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

public class QuestLeaveConfirmedCommand extends AdvancedCommand {
   public QuestLeaveConfirmedCommand() {
      super(List.of("quest"));
      this.addLiteral("leaveconfirmed");
      this.addArgument("questID", new ListStringCommandArgument("<questID>"));
      this.setUsage("/em quest leaveconfirmed");
      this.setPermission("elitemobs.quest.leave");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Confirms leaving a quest. Used via the confirmation prompt, can't be directly used.");
   }

   @Override
   public void execute(CommandData commandData) {
      QuestCommand.leaveQuestConfirmed(commandData.getPlayerSender(), commandData.getStringArgument("questID"));
   }
}

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

public class QuestLeaveCommand extends AdvancedCommand {
   public QuestLeaveCommand() {
      super(List.of("quest"));
      this.addLiteral("leave");
      this.addArgument("questID", new ListStringCommandArgument("<questID>"));
      this.setUsage("/em quest leave");
      this.setPermission("elitemobs.quest.leave");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Leaves a quest. Used via menu, can't be directly used.");
   }

   @Override
   public void execute(CommandData commandData) {
      QuestCommand.leaveQuest(commandData.getPlayerSender(), commandData.getStringArgument("questID"));
   }
}

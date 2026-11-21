package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.ListStringCommandArgument;
import java.util.List;

public class QuestTrackCommand extends AdvancedCommand {
   public QuestTrackCommand() {
      super(List.of("quest"));
      this.addLiteral("track");
      this.addArgument("questID", new ListStringCommandArgument("<questID>"));
      this.setUsage("/em quest track <questID>");
      this.setPermission("elitemobs.quest.track");
      this.setSenderType(SenderType.PLAYER);
      this.setDescription("Tracks a quest. Used via menu, can't be directly used.");
   }

   @Override
   public void execute(CommandData commandData) {
      QuestCommand.trackQuest(commandData.getStringArgument("questID"), commandData.getPlayerSender());
   }
}

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class QuestResetAllCommand extends AdvancedCommand {
   public QuestResetAllCommand() {
      super(List.of("quest"));
      this.addLiteral("resetAll");
      this.addArgument("player", new PlayerCommandArgument());
      this.setUsage("/em quest resetAll <player>");
      this.setPermission("elitemobs.quest.reset.all");
      this.setDescription("Resets all quests for a specific player.");
   }

   @Override
   public void execute(CommandData commandData) {
      QuestCommand.resetQuests(commandData.getCommandSender(), commandData.getStringArgument("player"));
   }
}

package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.commands.quests.QuestCommand;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.magmacore.command.AdvancedCommand;
import com.magmaguy.elitemobs.magmacore.command.CommandData;
import com.magmaguy.elitemobs.magmacore.command.SenderType;
import com.magmaguy.elitemobs.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.elitemobs.magmacore.command.arguments.PlayerCommandArgument;
import java.util.List;

public class QuestResetCommand extends AdvancedCommand {
   public QuestResetCommand() {
      super(List.of("quest"));
      this.addLiteral("reset");
      this.addArgument("player", new PlayerCommandArgument());
      this.addArgument("questName", new DynamicListStringCommandArgument(() -> CustomQuestsConfig.getCustomQuests().keySet().stream().toList(), "<questName>"));
      this.setUsage("/em quest reset <player> <quest filename>");
      this.setPermission("elitemobs.quest.reset");
      this.setDescription("Resets a specific quest for a specific player.");
      this.setSenderType(SenderType.PLAYER);
   }

   @Override
   public void execute(CommandData commandData) {
      QuestCommand.resetQuest(commandData.getCommandSender(), commandData.getStringArgument("player"), commandData.getStringArgument("questName"));
   }
}

package com.magmaguy.elitemobs.commands.quests;

import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.magmacore.util.SpigotMessage;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.playercooldowns.PlayerQuestCooldowns;
import java.util.ArrayList;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuestCommand {
   private QuestCommand() {
   }

   public static void completeQuest(String questID, Player player) {
      try {
         Quest.completeQuest(UUID.fromString(questID), player);
      } catch (Exception var3) {
         player.sendMessage(CommandMessagesConfig.getQuestInvalidIdMessage());
      }
   }

   public static void joinQuest(String questID, Player player) {
      CustomQuest.startQuest(questID, player);
   }

   public static void trackQuest(String questID, Player player) {
      QuestTracking.toggleTracking(player, questID);
   }

   public static void leaveQuest(Player player, String questID) {
      Quest quest = PlayerData.getQuest(player.getUniqueId(), questID);
      if (quest == null) {
         Quest.stopPlayerQuest(player, questID);
      } else {
         player.spigot()
            .sendMessage(
               SpigotMessage.commandHoverMessage(
                  QuestsConfig.getQuestAbandonConfirmationText().replace("$questName", quest.getQuestName() == null ? "" : quest.getQuestName()),
                  QuestsConfig.getQuestAbandonConfirmationHover(),
                  "/elitemobs quest leaveconfirmed " + questID
               )
            );
      }
   }

   public static void leaveQuestConfirmed(Player player, String questID) {
      Quest.stopPlayerQuest(player, questID);
   }

   public static void resetQuests(CommandSender commandSender, String playerString) {
      Player player = Bukkit.getPlayer(playerString);
      if (player == null) {
         commandSender.sendMessage(CommandMessagesConfig.getQuestPlayerNotValidMessage());
      } else {
         PlayerQuestCooldowns.resetPlayerQuests(player);
         commandSender.sendMessage(CommandMessagesConfig.getQuestResetSuccessMessage().replace("$player", playerString));
      }
   }

   public static void resetQuest(CommandSender commandSender, String playerString, String questName) {
      Player player = Bukkit.getPlayer(playerString);
      if (player == null) {
         commandSender.sendMessage(CommandMessagesConfig.getQuestPlayerNotValidMessage());
      } else {
         CustomQuestsConfigFields customQuestsConfigFields = CustomQuestsConfig.getCustomQuests().get(questName);
         if (customQuestsConfigFields == null) {
            commandSender.sendMessage(CommandMessagesConfig.getQuestNameNotValidMessage().replace("$quest", questName));
         } else {
            PlayerQuestCooldowns.resetPlayerQuestCooldown(player, customQuestsConfigFields);
            commandSender.sendMessage(CommandMessagesConfig.getQuestCompletedSuccessMessage().replace("$quest", questName).replace("$player", playerString));
         }
      }
   }

   public static void completeQuest(Player player) {
      for (Quest quest : new ArrayList<>(PlayerData.getQuests(player.getUniqueId()))) {
         quest.getQuestObjectives().setForceOver(true);
         Quest.completeQuest(quest.getQuestID(), player);
      }
   }

   public static void bypassQuestRequirements(Player player) {
      PlayerQuestCooldowns.toggleBypass(player);
   }
}

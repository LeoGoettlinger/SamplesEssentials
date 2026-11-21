package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.api.QuestRewardEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class QuestLockoutHandler implements Listener {
   public static boolean isLockedOut(Player player, String questFilename, String questName) {
      QuestLockout questLockout = PlayerData.getQuestLockout(player.getUniqueId());
      if (questLockout == null) {
         return false;
      } else if (questLockout.isLockedOut(questFilename)) {
         notifyLockout(player, questName, questLockout, questFilename);
         return true;
      } else {
         return false;
      }
   }

   private static void notifyLockout(Player player, String questName, QuestLockout questLockout, String questFilename) {
      String remainingTime = questLockout.getFormattedRemainingTime(questFilename);
      String subtitle = QuestsConfig.getQuestLockoutSubtitle();
      ActionBarCompositor.show(player, ActionBarCompositor.Source.LOCKOUT, subtitle);
      player.sendTitle("", subtitle, 10, 70, 20);
      String chatMessage = QuestsConfig.getQuestLockoutChatMessage().replace("$questName", questName).replace("$remainingTime", remainingTime);
      player.sendMessage(chatMessage);
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuestReward(QuestRewardEvent event) {
      if (event.getQuest() instanceof CustomQuest customQuest) {
         CustomQuestsConfigFields config = customQuest.getCustomQuestsConfigFields();
         if (config != null) {
            int lockoutMinutes = config.getQuestLockoutMinutes();
            if (lockoutMinutes > 0) {
               Player player = event.getPlayer();
               String questFilename = customQuest.getConfigurationFilename();
               QuestLockout questLockout = PlayerData.getQuestLockout(player.getUniqueId());
               if (questLockout == null) {
                  questLockout = new QuestLockout();
               }

               questLockout.addLockout(questFilename, lockoutMinutes);
               PlayerData.updateQuestLockout(player.getUniqueId(), questLockout);
            }
         }
      }
   }
}

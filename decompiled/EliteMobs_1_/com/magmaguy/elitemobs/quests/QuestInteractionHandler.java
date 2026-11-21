package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfig;
import com.magmaguy.elitemobs.config.customquests.CustomQuestsConfigFields;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.quests.menus.QuestMenu;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class QuestInteractionHandler {
   private QuestInteractionHandler() {
   }

   public static void processDynamicQuests(Player player, NPCEntity npcEntity) {
      if (PlayerData.isInMemory(player) && npcEntity != null) {
         List<DynamicQuest> dynamicQuests = new ArrayList<>();
         DynamicQuest.getQuests(player).forEach(quest -> {
            if (!quest.getQuestObjectives().isTurnedIn()) {
               dynamicQuests.add(quest);
            }
         });
         if (!dynamicQuests.isEmpty()
            && !QuestDialogueBossBarManager.showQuestMenuIntro(
               dynamicQuests, player, npcEntity, () -> QuestMenu.generateDynamicQuestMenu(dynamicQuests, player, npcEntity)
            )) {
            QuestMenu.generateDynamicQuestMenu(dynamicQuests, player, npcEntity);
         }
      }
   }

   public static void processNPCQuests(final Player player, final NPCEntity npcEntity) {
      if (PlayerData.isInMemory(player) && npcEntity != null) {
         final List<CustomQuest> customQuestList = new ArrayList<>();
         List<Quest> quests = PlayerData.getQuests(player.getUniqueId());
         scanQuestTakerNPC(npcEntity, quests, customQuestList, player);
         List<String> invalidQuests = new ArrayList<>();
         CustomQuestsConfigFields lockedQuest = null;
         int questCompleteCount = 0;
         if (npcEntity.getNPCsConfigFields().getQuestFilenames() != null) {
            for (String questString : npcEntity.getNPCsConfigFields().getQuestFilenames()) {
               boolean activeQuest = false;

               for (CustomQuest customQuest : customQuestList) {
                  if (customQuest.getCustomQuestsConfigFields().getFilename().equals(questString)) {
                     activeQuest = true;
                  }
               }

               if (!activeQuest) {
                  CustomQuestsConfigFields fields = CustomQuestsConfig.getCustomQuests().get(questString);
                  if (fields == null) {
                     invalidQuests.add(questString);
                  } else {
                     CustomQuest customQuestx = CustomQuest.getQuest(questString, player);
                     if (customQuestx != null && !customQuestx.getQuestObjectives().isTurnedIn()) {
                        customQuestList.add(customQuestx);
                        customQuestx.setQuestGiver(npcEntity.getNPCsConfigFields().getFilename());
                     } else {
                        String lockoutPerm = fields.getQuestLockoutPermission();
                        boolean completedViaPermission = lockoutPerm != null && !lockoutPerm.isEmpty() && player.hasMetadata(lockoutPerm);
                        QuestLockout lockout = PlayerData.getQuestLockout(player.getUniqueId());
                        boolean completedViaLockout = fields.getQuestLockoutMinutes() > 0 && lockout != null && lockout.isLockedOut(fields.getFilename());
                        if (completedViaLockout && lockedQuest == null) {
                           lockedQuest = fields;
                        }

                        if (completedViaPermission || completedViaLockout) {
                           questCompleteCount++;
                        }
                     }
                  }
               }
            }
         }

         Quest.replaceOffers(player, customQuestList);
         if (!customQuestList.isEmpty()) {
            (new BukkitRunnable() {
                  public void run() {
                     if (player.isOnline() && PlayerData.isInMemory(player)) {
                        if (!QuestDialogueBossBarManager.showQuestMenuIntro(
                           customQuestList, player, npcEntity, () -> QuestMenu.generateCustomQuestMenu(customQuestList, player, npcEntity)
                        )) {
                           QuestMenu.generateCustomQuestMenu(customQuestList, player, npcEntity);
                        }
                     }
                  }
               })
               .runTaskLater(MetadataHandler.PLUGIN, 1L);
         } else {
            if (!invalidQuests.isEmpty()) {
               player.sendMessage(QuestsConfig.getInvalidQuestNpcMessage());
               if (player.hasPermission("elitemobs.*")) {
                  for (String invalidQuest : invalidQuests) {
                     player.sendMessage(QuestsConfig.getInvalidQuestMessage().replace("$quest", invalidQuest));
                  }
               }
            } else if (lockedQuest != null) {
               QuestLockoutHandler.isLockedOut(player, lockedQuest.getFilename(), lockedQuest.getQuestName());
            } else if (npcEntity.getNPCsConfigFields().getQuestFilenames() != null && !npcEntity.getNPCsConfigFields().getQuestFilenames().isEmpty()) {
               if (questCompleteCount == npcEntity.getNPCsConfigFields().getQuestFilenames().size()) {
                  player.sendMessage(QuestsConfig.getQuestAlreadyCompletedMessage());
               } else {
                  player.sendMessage(QuestsConfig.getQuestPrerequisitesMissingMessage());
               }
            }
         }
      }
   }

   private static void scanQuestTakerNPC(NPCEntity npcEntity, List<Quest> activeQuests, List<CustomQuest> npcQuests, Player player) {
      for (Quest quest : activeQuests) {
         if (quest instanceof CustomQuest
            && quest.isAccepted()
            && !quest.getQuestObjectives().isTurnedIn()
            && quest.getQuestTaker().equals(npcEntity.getNPCsConfigFields().getFilename())) {
            npcQuests.add((CustomQuest)quest);

            for (Objective objective : quest.getQuestObjectives().getObjectives()) {
               if (objective instanceof CustomFetchObjective) {
                  objective.progressNonlinearObjective(quest.getQuestObjectives(), player);
               }
            }
         }
      }
   }
}

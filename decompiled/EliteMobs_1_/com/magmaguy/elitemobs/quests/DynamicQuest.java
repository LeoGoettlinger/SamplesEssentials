package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.menus.premade.DynamicQuestMenuConfig;
import com.magmaguy.elitemobs.mobconstructor.mobdata.aggressivemobs.EliteMobProperties;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.DynamicKillObjective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class DynamicQuest extends Quest {
   private static final long serialVersionUID = -2841343780852205224L;
   private static final HashMap<Integer, List<QuestObjectives>> threeRandomDynamicObjectives = new HashMap<>();
   private static BukkitTask randomizerTask;

   public DynamicQuest(Player player, int targetMobLevel, QuestObjectives questObjectives) {
      super(player, questObjectives, DynamicQuestLevel.clamp(targetMobLevel));
      DynamicKillObjective dynamicKillObjective = (DynamicKillObjective)questObjectives.getObjectives().get(0);
      super.questName = DynamicQuestMenuConfig.getQuestName()
         .replace("$amount", questObjectives.getObjectives().get(0).getTargetAmount() + "")
         .replace(
            "$name",
            ChatColor.stripColor(EliteMobProperties.getPluginData(dynamicKillObjective.getEntityType()).getName(dynamicKillObjective.getMinMobLevel()))
         );
      questObjectives.setQuest(this);
   }

   public static void startRandomizingQuests() {
      randomizerTask = (new BukkitRunnable() {
         public void run() {
            if (QuestsConfig.getQuestEntityTypes().isEmpty()) {
               DynamicQuest.threeRandomDynamicObjectives.clear();
            } else {
               for (int activeLevel = 1; activeLevel < 21; activeLevel++) {
                  List<QuestObjectives> questObjectives = new ArrayList<>();

                  for (int questNumber = 0; questNumber < 3; questNumber++) {
                     questObjectives.add(new QuestObjectives(activeLevel));
                  }

                  DynamicQuest.threeRandomDynamicObjectives.put(activeLevel, questObjectives);
               }
            }
         }
      }).runTaskTimer(MetadataHandler.PLUGIN, 0L, 1200L);
   }

   public static void shutdown() {
      threeRandomDynamicObjectives.clear();
      if (randomizerTask != null) {
         randomizerTask.cancel();
      }
   }

   public static List<DynamicQuest> generateQuests(Player player) {
      if (!PlayerData.isInMemory(player)) {
         return List.of();
      } else {
         int combatLevel = DynamicQuestLevel.clamp(CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
         int questTemplateBucket = DynamicQuestLevel.toTemplateBucket(combatLevel);
         if (questTemplateBucket == 0) {
            player.sendMessage(QuestsConfig.getLowRankDynamicQuestWarning());
            return new ArrayList<>();
         } else {
            List<DynamicQuest> dynamicQuests = new ArrayList<>();
            List<QuestObjectives> questTemplates = threeRandomDynamicObjectives.get(questTemplateBucket);
            if (questTemplates == null) {
               return dynamicQuests;
            } else {
               for (QuestObjectives questTemplate : questTemplates) {
                  if (!PlayerData.getQuests(player.getUniqueId())
                     .stream()
                     .anyMatch(quest -> quest instanceof DynamicQuest && quest.getQuestObjectives().getUuid().equals(questTemplate.getUuid()))) {
                     DynamicQuest existing = null;

                     for (Quest pending : pendingPlayerQuests.getOrDefault(player.getUniqueId(), List.of())) {
                        if (pending instanceof DynamicQuest dynamic
                           && dynamic.getQuestLevel() == combatLevel
                           && dynamic.getQuestObjectives().getUuid().equals(questTemplate.getUuid())) {
                           existing = dynamic;
                           break;
                        }
                     }

                     if (existing != null) {
                        dynamicQuests.add(existing);
                     } else {
                        DynamicKillObjective templateObjective = (DynamicKillObjective)questTemplate.getObjectives().get(0);
                        QuestObjectives playerQuestObjectives = new QuestObjectives(
                           questTemplate.getUuid(),
                           List.of(new DynamicKillObjective(templateObjective.getTargetAmount(), templateObjective.getEntityType(), combatLevel))
                        );
                        QuestReward questReward = new QuestReward(combatLevel, playerQuestObjectives, player);
                        playerQuestObjectives.setQuestReward(questReward);
                        dynamicQuests.add(new DynamicQuest(player, combatLevel, playerQuestObjectives));
                     }
                  }
               }

               return dynamicQuests;
            }
         }
      }
   }

   public static List<DynamicQuest> getQuests(Player player) {
      if (!PlayerData.isInMemory(player)) {
         return List.of();
      } else {
         List<DynamicQuest> dynamicQuests = new ArrayList<>();
         if (PlayerData.getQuests(player.getUniqueId()) != null) {
            for (Quest quest : PlayerData.getQuests(player.getUniqueId())) {
               if (quest instanceof DynamicQuest dynamicQuest) {
                  dynamicQuests.add(dynamicQuest);
               }
            }
         }

         dynamicQuests.addAll(generateQuests(player));
         replaceOffers(player, dynamicQuests);
         return dynamicQuests;
      }
   }

   public static boolean hasAvailableQuests(Player player) {
      return !PlayerData.isInMemory(player) ? false : hasAvailableQuests(player, PlayerData.getQuests(player.getUniqueId()));
   }

   public static boolean hasAvailableQuests(Player player, List<Quest> activeQuests) {
      if (activeQuests == null) {
         return false;
      } else {
         for (Quest quest : activeQuests) {
            if (quest instanceof DynamicQuest && !quest.isAccepted() && !quest.getQuestObjectives().isTurnedIn()) {
               return true;
            }
         }

         int level = DynamicQuestLevel.clamp(CombatLevelCalculator.calculateCombatLevel(player.getUniqueId()));
         List<QuestObjectives> templates = threeRandomDynamicObjectives.get(DynamicQuestLevel.toTemplateBucket(level));
         if (templates == null) {
            return false;
         } else {
            for (QuestObjectives template : templates) {
               if (activeQuests.stream()
                  .noneMatch(questx -> questx instanceof DynamicQuest && questx.getQuestObjectives().getUuid().equals(template.getUuid()))) {
                  return true;
               }
            }

            return false;
         }
      }
   }

   public static void adaptPlayerQuestsToLevel(Player player, int newMobLevel) {
      if (PlayerData.getQuests(player.getUniqueId()) != null) {
         for (Quest quest : PlayerData.getQuests(player.getUniqueId())) {
            if (quest instanceof DynamicQuest dynamicQuest) {
               dynamicQuest.adaptToLevel(newMobLevel);
            }
         }
      }
   }

   public void adaptToLevel(int newMobLevel) {
      int cappedMobLevel = DynamicQuestLevel.clamp(newMobLevel);
      this.setQuestLevel(cappedMobLevel);
      this.getQuestObjectives().adaptToLevel(cappedMobLevel);
      if (!this.getQuestObjectives().getObjectives().isEmpty()
         && this.getQuestObjectives().getObjectives().get(0) instanceof DynamicKillObjective dynamicKillObjective) {
         this.questName = DynamicQuestMenuConfig.getQuestName()
            .replace("$amount", dynamicKillObjective.getTargetAmount() + "")
            .replace("$name", ChatColor.stripColor(EliteMobProperties.getPluginData(dynamicKillObjective.getEntityType()).getName(cappedMobLevel)));
      }
   }
}

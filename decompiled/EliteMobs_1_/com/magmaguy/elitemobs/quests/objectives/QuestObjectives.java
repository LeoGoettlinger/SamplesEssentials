package com.magmaguy.elitemobs.quests.objectives;

import com.magmaguy.elitemobs.api.QuestObjectivesCompletedEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfig;
import com.magmaguy.elitemobs.config.npcs.NPCsConfigFields;
import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import com.magmaguy.elitemobs.parties.PartyManager;
import com.magmaguy.elitemobs.parties.PartySidebar;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.dialogue.QuestDialogueBossBarManager;
import com.magmaguy.elitemobs.quests.rewards.QuestReward;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.SimpleScoreboard;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class QuestObjectives implements Serializable {
   private static final long serialVersionUID = -6402858811214459738L;
   private final UUID uuid;
   protected QuestReward questReward;
   private List<Objective> objectives;
   private Quest quest;
   private boolean over = false;
   private boolean turnedIn = false;
   private boolean forceOver = false;

   public QuestObjectives(int questLevel) {
      this.uuid = UUID.randomUUID();
      this.generateRandomObjective(questLevel);
   }

   public QuestObjectives(UUID uuid, List<Objective> objectives) {
      this.uuid = uuid;
      this.objectives = objectives;
   }

   public QuestObjectives(QuestReward customQuestReward) {
      this.uuid = UUID.randomUUID();
      this.questReward = customQuestReward;
   }

   private void generateRandomObjective(int questLevel) {
      int killAmount = ThreadLocalRandom.current().nextInt(1 + questLevel, 1 + questLevel * 10);
      EntityType entityType = QuestsConfig.getQuestEntityTypes().get(ThreadLocalRandom.current().nextInt(QuestsConfig.getQuestEntityTypes().size()));
      KillObjective killObjective = new DynamicKillObjective(killAmount, entityType, questLevel * 10);
      this.objectives = Collections.singletonList(killObjective);
   }

   public void setQuest(Quest quest) {
      this.quest = quest;
      if (quest instanceof CustomQuest) {
         this.objectives = CustomObjectivesParser.processCustomObjectives((CustomQuest)quest);
      }
   }

   public boolean isOver() {
      if (this.forceOver) {
         return true;
      } else {
         boolean checkOver = true;

         for (Objective objective : this.objectives) {
            if (objective == null) {
               return false;
            }

            if (!objective.isObjectiveCompleted()) {
               checkOver = false;
               break;
            }
         }

         return checkOver;
      }
   }

   public void updateQuestStatus(UUID playerUUID) {
      if (!this.turnedIn) {
         if (!this.over) {
            if (this.isOver()) {
               this.over = true;
               QuestObjectivesCompletedEvent questObjectivesCompletedEvent = new QuestObjectivesCompletedEvent(Bukkit.getPlayer(playerUUID), this.quest);
               new EventCaller(questObjectivesCompletedEvent);
            }
         }
      }
   }

   public void displayTemporaryObjectivesScoreboard(Player player) {
      if (QuestsConfig.isUseQuestScoreboards()) {
         if (!this.scoreboardHiddenForDialogue(player)) {
            if (PartySidebar.isEnabled() && PartyManager.isInParty(player.getUniqueId())) {
               PartySidebar.showTemporaryQuest(player, ChatColorConverter.convert(this.getQuest().getQuestName()), this.getScoreboardObjectiveText(), 400);
            } else {
               SimpleScoreboard.temporaryScoreboard(player, ChatColorConverter.convert(this.getQuest().getQuestName()), this.getScoreboardObjectiveText(), 400);
            }
         }
      }
   }

   public void displayLazyObjectivesScoreboard(Player player) {
      if (QuestsConfig.isUseQuestScoreboards()) {
         if (!this.scoreboardHiddenForDialogue(player)) {
            if (PartySidebar.isEnabled() && PartyManager.isInParty(player.getUniqueId())) {
               PartySidebar.refresh(player);
            } else {
               SimpleScoreboard.updateScoreboard(player, ChatColorConverter.convert(this.getQuest().getQuestName()), this.getScoreboardObjectiveText());
            }
         }
      }
   }

   private boolean scoreboardHiddenForDialogue(Player player) {
      return QuestsConfig.isHideQuestScoreboardDuringQuestDialogue() && QuestDialogueBossBarManager.hasActiveSession(player);
   }

   public List<String> getScoreboardObjectiveText() {
      List<String> strings = new ArrayList<>();
      if (!this.isOver()) {
         for (Objective objective : this.objectives) {
            if (objective != null) {
               strings.add(QuestsConfig.getQuestScoreboardProgressionLine(objective));
            }
         }
      } else {
         if (this.quest.getQuestTaker() == null) {
            return strings;
         }

         NPCsConfigFields npCsConfigFields = NPCsConfig.getNpcEntities().get(this.quest.getQuestTaker());
         if (npCsConfigFields == null) {
            return strings;
         }

         strings.add(QuestsConfig.getQuestTurnInObjective().replace("$npcName", npCsConfigFields.getName() == null ? "" : npCsConfigFields.getName()));
      }

      return strings;
   }

   public void adaptToLevel(int newMobLevel) {
      for (Objective objective : this.objectives) {
         if (objective instanceof DynamicKillObjective dynamicKillObjective) {
            dynamicKillObjective.adaptToLevel(newMobLevel);
         }
      }
   }

   @Generated
   public UUID getUuid() {
      return this.uuid;
   }

   @Generated
   public QuestReward getQuestReward() {
      return this.questReward;
   }

   @Generated
   public void setQuestReward(QuestReward questReward) {
      this.questReward = questReward;
   }

   @Generated
   public List<Objective> getObjectives() {
      return this.objectives;
   }

   @Generated
   public void setObjectives(List<Objective> objectives) {
      this.objectives = objectives;
   }

   @Generated
   public Quest getQuest() {
      return this.quest;
   }

   @Generated
   public boolean isTurnedIn() {
      return this.turnedIn;
   }

   @Generated
   public void setTurnedIn(boolean turnedIn) {
      this.turnedIn = turnedIn;
   }

   @Generated
   public void setForceOver(boolean forceOver) {
      this.forceOver = forceOver;
   }
}

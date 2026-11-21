package com.magmaguy.elitemobs.quests;

import com.magmaguy.elitemobs.api.QuestCompleteEvent;
import com.magmaguy.elitemobs.api.QuestLeaveEvent;
import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.quests.objectives.QuestObjectives;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Quest implements Serializable {
   private static final long serialVersionUID = -2272809707714188608L;
   protected static final HashMap<UUID, List<Quest>> pendingPlayerQuests = new HashMap<>();
   protected final QuestObjectives questObjectives;
   private final UUID questID = UUID.randomUUID();
   protected String questName;
   protected String questGiver = "";
   protected String questTaker = "";
   private UUID playerUUID;
   private int questLevel;
   private boolean accepted = false;

   public static void shutdown() {
      for (Player player : Bukkit.getOnlinePlayers()) {
         releasePlayerSession(player.getUniqueId());
      }

      pendingPlayerQuests.clear();
   }

   public static void releasePlayerSession(UUID playerId) {
      CustomFetchObjective.cancelRefresh(playerId);
      pendingPlayerQuests.remove(playerId);
      if (PlayerData.isInMemory(playerId)) {
         for (Quest quest : PlayerData.getQuests(playerId)) {
            if (quest instanceof CustomQuest customQuest) {
               customQuest.releaseTemporaryPermissions();
            }
         }
      }
   }

   static void replaceOffers(Player player, List<? extends Quest> offers) {
      List<Quest> pending = new ArrayList<>();

      for (Quest quest : offers) {
         if (!quest.isAccepted() && !quest.getQuestObjectives().isTurnedIn()) {
            pending.add(quest);
         }
      }

      if (pending.isEmpty()) {
         pendingPlayerQuests.remove(player.getUniqueId());
      } else {
         pendingPlayerQuests.put(player.getUniqueId(), pending);
      }
   }

   public static boolean canAccept(Player player, Quest offer) {
      if (PlayerData.isInMemory(player)
         && player.isOnline()
         && offer != null
         && player.getUniqueId().equals(offer.getPlayerUUID())
         && !offer.isAccepted()
         && !offer.getQuestObjectives().isTurnedIn()) {
         if (offer instanceof CustomQuest custom && !custom.hasPermissionForQuest(player)) {
            return false;
         } else {
            for (Quest active : PlayerData.getQuests(player.getUniqueId())) {
               if (active.getQuestID().equals(offer.getQuestID())) {
                  return false;
               }

               if (offer instanceof CustomQuest custom
                  && active instanceof CustomQuest current
                  && custom.getConfigurationFilename().equals(current.getConfigurationFilename())) {
                  return false;
               }

               if (offer instanceof DynamicQuest
                  && active instanceof DynamicQuest
                  && offer.getQuestObjectives().getUuid().equals(active.getQuestObjectives().getUuid())) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public Quest(Player player, QuestObjectives questObjectives, int questLevel) {
      this.playerUUID = player.getUniqueId();
      this.questObjectives = questObjectives;
      this.questLevel = questLevel;
   }

   public static void stopPlayerQuest(Player player, String questID) {
      if (PlayerData.getQuests(player.getUniqueId()) != null && PlayerData.getQuest(player.getUniqueId(), questID) != null) {
         QuestLeaveEvent questLeaveEvent = new QuestLeaveEvent(player, PlayerData.getQuest(player.getUniqueId(), questID));
         new EventCaller(questLeaveEvent);
      } else {
         player.sendMessage(QuestsConfig.getLeaveWhenNoActiveQuestsExist());
      }
   }

   public static Quest completeQuest(UUID questUUID, Player player) {
      Quest quest = PlayerData.getQuest(player.getUniqueId(), questUUID);
      if (quest == null) {
         return null;
      } else if (!quest.getQuestID().equals(questUUID)) {
         return null;
      } else if (quest.getQuestObjectives().isTurnedIn()) {
         return null;
      } else if (!quest.getQuestObjectives().isOver()) {
         return null;
      } else {
         QuestCompleteEvent questCompleteEvent = new QuestCompleteEvent(player, quest);
         new EventCaller(questCompleteEvent);
         return questCompleteEvent.isCancelled() ? null : quest;
      }
   }

   public String getQuestTaker() {
      return this.questTaker.isEmpty() ? this.questGiver : this.questTaker;
   }

   @Generated
   public static HashMap<UUID, List<Quest>> getPendingPlayerQuests() {
      return pendingPlayerQuests;
   }

   @Generated
   public QuestObjectives getQuestObjectives() {
      return this.questObjectives;
   }

   @Generated
   public UUID getQuestID() {
      return this.questID;
   }

   @Generated
   public String getQuestName() {
      return this.questName;
   }

   @Generated
   public String getQuestGiver() {
      return this.questGiver;
   }

   @Generated
   public void setQuestGiver(String questGiver) {
      this.questGiver = questGiver;
   }

   @Generated
   public void setQuestTaker(String questTaker) {
      this.questTaker = questTaker;
   }

   @Generated
   public UUID getPlayerUUID() {
      return this.playerUUID;
   }

   @Generated
   public void setPlayerUUID(UUID playerUUID) {
      this.playerUUID = playerUUID;
   }

   @Generated
   public int getQuestLevel() {
      return this.questLevel;
   }

   @Generated
   public void setQuestLevel(int questLevel) {
      this.questLevel = questLevel;
   }

   @Generated
   public boolean isAccepted() {
      return this.accepted;
   }

   @Generated
   public void setAccepted(boolean accepted) {
      this.accepted = accepted;
   }
}

package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class QuestRewardEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final Quest quest;

   public QuestRewardEvent(Player player, Quest quest) {
      this.player = player;
      this.quest = quest;
      quest.getQuestObjectives().setTurnedIn(true);
      this.doMessages();
      this.doRewards();
      this.completeQuestData();
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   private void doMessages() {
      this.player.sendMessage(QuestsConfig.getQuestCompleteMessage().replace("$questName", this.quest.getQuestName()));
      if (QuestsConfig.isUseQuestCompleteTitles()) {
         this.player
            .sendTitle(
               QuestsConfig.getQuestCompleteTitle().replace("$questName", this.quest.getQuestName()),
               QuestsConfig.getQuestCompleteSubtitle().replace("$questName", this.quest.getQuestName()),
               20,
               60,
               20
            );
      }
   }

   private void doRewards() {
      this.quest.getQuestObjectives().getQuestReward().doRewards(this.player);
   }

   private void completeQuestData() {
      PlayerData.removeQuest(this.player.getUniqueId(), this.quest);
      PlayerData.incrementQuestsCompleted(this.player.getUniqueId());
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public Quest getQuest() {
      return this.quest;
   }
}

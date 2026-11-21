package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class QuestLeaveEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final Quest quest;

   public QuestLeaveEvent(Player player, Quest quest) {
      this.player = player;
      this.quest = quest;
   }

   public static HandlerList getHandlerList() {
      return handlers;
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

   public static class QuestLeaveEventHandler implements Listener {
      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.MONITOR
      )
      public void onQuestLeave(QuestLeaveEvent event) {
         PlayerData.removeQuest(event.getPlayer().getUniqueId(), event.getQuest());
         event.getPlayer().sendMessage(QuestsConfig.getQuestLeaveMessage().replace("$questName", event.getQuest().getQuestName()));
         if (QuestsConfig.isUseQuestLeaveTitles()) {
            event.getPlayer()
               .sendTitle(
                  QuestsConfig.getQuestLeaveTitle().replace("$questName", event.getQuest().getQuestName()),
                  QuestsConfig.getQuestLeaveSubtitle().replace("$questName", event.getQuest().getQuestName()),
                  20,
                  60,
                  20
               );
         }

         QuestTracking questTracking = QuestTracking.getPlayerTrackingQuests().get(event.getPlayer().getUniqueId());
         if (questTracking != null && questTracking.getQuest().getQuestID().equals(event.getQuest().getQuestID())) {
            questTracking.stop();
         }

         event.getPlayer().playSound(event.getPlayer().getLocation(), SoundsConfig.questAbandonSound, 1.0F, 1.0F);
      }
   }
}

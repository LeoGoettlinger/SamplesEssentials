package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class QuestObjectivesCompletedEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final Quest quest;

   public QuestObjectivesCompletedEvent(Player player, Quest customQuest) {
      this.player = player;
      this.quest = customQuest;
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

   public static class QuestObjectivesCompletedEventHandler implements Listener {
      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.MONITOR
      )
      public void onObjectivesCompleted(QuestObjectivesCompletedEvent event) {
         if (!QuestsConfig.isRequireQuestTurnIn()) {
            QuestCompleteEvent questCompleteEvent = new QuestCompleteEvent(event.getPlayer(), event.getQuest());
            new EventCaller(questCompleteEvent);
         }
      }
   }
}

package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.objectives.CustomFetchObjective;
import com.magmaguy.elitemobs.utils.EventCaller;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class QuestCompleteEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final Quest quest;
   private boolean cancelled;

   public QuestCompleteEvent(Player player, Quest quest) {
      this.player = player;
      this.quest = quest;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public Quest getQuest() {
      return this.quest;
   }

   public static class QuestCompleteEventHandler implements Listener {
      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.MONITOR
      )
      public void onQuestComplete(QuestCompleteEvent event) {
         if (!event.quest.getQuestObjectives().isTurnedIn() && CustomFetchObjective.prepareTurnIn(event.quest.getQuestObjectives(), event.getPlayer(), true)) {
            new EventCaller(new QuestRewardEvent(event.getPlayer(), event.quest));
            if (event.getQuest() instanceof CustomQuest customQuest && customQuest.getCustomQuestsConfigFields().getQuestCompleteSound() != null) {
               event.getPlayer().playSound(event.getPlayer(), customQuest.getCustomQuestsConfigFields().getQuestCompleteSound(), 1.0F, 1.0F);
            } else {
               event.getPlayer().playSound(event.getPlayer().getLocation(), SoundsConfig.questCompleteSound, 1.0F, 1.0F);
            }
         } else {
            event.setCancelled(true);
         }
      }
   }
}

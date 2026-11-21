package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.Quest;
import com.magmaguy.elitemobs.quests.QuestTracking;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.MessageThrottler;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class QuestProgressionEvent extends Event {
   private static final Set<UUID> batchingPlayers = new HashSet<>();
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final Quest quest;
   private final Objective objective;

   public QuestProgressionEvent(Player player, Quest quest, Objective objective) {
      this.player = player;
      this.quest = quest;
      this.objective = objective;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public static void fireBatch(Player player, List<QuestProgressionEvent> events) {
      if (!events.isEmpty()) {
         boolean ownsBatch = batchingPlayers.add(player.getUniqueId());

         try {
            for (QuestProgressionEvent event : events) {
               if (!event.quest.getQuestObjectives().isTurnedIn()) {
                  new EventCaller(event);
               }
            }
         } finally {
            if (ownsBatch) {
               batchingPlayers.remove(player.getUniqueId());
               if (PlayerData.isInMemory(player)) {
                  PlayerData.updateQuestStatus(player.getUniqueId());
               }
            }
         }
      }
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

   @Generated
   public Objective getObjective() {
      return this.objective;
   }

   public static class QuestProgressionEventHandler implements Listener {
      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.MONITOR
      )
      public void onQuestProgression(QuestProgressionEvent event) {
         event.getQuest().getQuestObjectives().updateQuestStatus(event.getPlayer().getUniqueId());
         if (!event.getQuest().getQuestObjectives().isTurnedIn()) {
            if (QuestsConfig.isDoQuestChatProgression()) {
               MessageThrottler.pushQuestProgress(event.getPlayer(), event.getObjective(), QuestsConfig.getQuestChatProgressionMessage(event.getObjective()));
            }

            if (!QuestTracking.isTracking(event.player)) {
               event.getQuest().getQuestObjectives().displayTemporaryObjectivesScoreboard(event.getPlayer());
            }

            if (!QuestProgressionEvent.batchingPlayers.contains(event.getPlayer().getUniqueId())) {
               PlayerData.updateQuestStatus(event.getPlayer().getUniqueId(), event.getQuest());
            }

            event.getPlayer().playSound(event.getPlayer().getLocation(), SoundsConfig.questProgressionSound, 1.0F, 1.0F);
         }
      }
   }
}

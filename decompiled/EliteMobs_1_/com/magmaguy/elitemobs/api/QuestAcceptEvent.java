package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.config.QuestsConfig;
import com.magmaguy.elitemobs.config.SoundsConfig;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.quests.CustomQuest;
import com.magmaguy.elitemobs.quests.Quest;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public class QuestAcceptEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final Quest quest;
   private boolean isCancelled = false;

   public QuestAcceptEvent(Player player, Quest quest) {
      this.player = player;
      this.quest = quest;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean cancel) {
      this.isCancelled = cancel;
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

   public static class QuestAcceptEventHandler implements Listener {
      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.LOWEST
      )
      public void questAcceptLimiter(QuestAcceptEvent event) {
         if (!Quest.canAccept(event.player, event.quest)) {
            event.setCancelled(true);
         } else if (PlayerData.getQuests(event.getPlayer().getUniqueId()).size() >= QuestsConfig.getMaximumActiveQuests()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(QuestsConfig.getQuestCapMessage());
         }
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.MONITOR
      )
      public void onQuestAccept(QuestAcceptEvent event) {
         if (!Quest.canAccept(event.player, event.quest)) {
            event.setCancelled(true);
         } else {
            event.getQuest().setAccepted(true);
            PlayerData.addQuest(event.getPlayer().getUniqueId(), event.getQuest());
            event.getPlayer().sendMessage(QuestsConfig.getQuestJoinMessage().replace("$questName", event.getQuest().getQuestName()));
            if (QuestsConfig.isUseQuestAcceptTitles()) {
               event.getPlayer()
                  .sendTitle(
                     QuestsConfig.getQuestStartTitle().replace("$questName", event.getQuest().getQuestName()),
                     QuestsConfig.getQuestStartSubtitle().replace("$questName", event.getQuest().getQuestName()),
                     20,
                     60,
                     20
                  );
            }

            boolean playedCustomSound = false;
            if (event.getQuest() instanceof CustomQuest customQuest) {
               customQuest.applyTemporaryPermissions(event.getPlayer());
               if (customQuest.getCustomQuestsConfigFields().getQuestAcceptDialog() != null
                  && !customQuest.getCustomQuestsConfigFields().getQuestAcceptDialog().isEmpty()) {
                  for (String dialog : customQuest.getCustomQuestsConfigFields().getQuestAcceptDialog()) {
                     event.getPlayer().sendMessage(dialog);
                  }
               }

               if (customQuest.getCustomQuestsConfigFields().getQuestAcceptSound() != null) {
                  Bukkit.getPlayer(customQuest.getPlayerUUID())
                     .playSound(Bukkit.getPlayer(customQuest.getPlayerUUID()), customQuest.getCustomQuestsConfigFields().getQuestAcceptSound(), 1.0F, 1.0F);
                  playedCustomSound = true;
               }
            }

            if (!QuestsConfig.isAutoTrackQuestsOnAccept()) {
               event.getQuest().getQuestObjectives().displayTemporaryObjectivesScoreboard(event.getPlayer());
            }

            if (!playedCustomSound) {
               event.getPlayer().playSound(event.getPlayer().getLocation(), SoundsConfig.questAcceptSound, 1.0F, 1.0F);
            }
         }
      }
   }
}

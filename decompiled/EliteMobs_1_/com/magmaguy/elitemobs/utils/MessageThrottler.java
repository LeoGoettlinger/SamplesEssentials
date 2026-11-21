package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.magmacore.util.Round;
import com.magmaguy.elitemobs.presentation.actionbar.ActionBarCompositor;
import com.magmaguy.elitemobs.quests.objectives.Objective;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public final class MessageThrottler {
   private static final Map<UUID, Map<String, MessageThrottler.PendingMessage>> pending = new HashMap<>();
   private static BukkitTask task;

   private MessageThrottler() {
   }

   public static void start() {
      if (task == null) {
         task = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, MessageThrottler::tick, 20L, 20L);
      }
   }

   public static void shutdown() {
      if (task != null) {
         task.cancel();
         task = null;
      }

      pending.clear();
   }

   public static double pushCurrency(Player player, double amount) {
      if (player == null) {
         return 0.0;
      } else {
         MessageThrottler.PendingMessage msg = getOrCreate(player, "currency", MessageThrottler.PolicyType.CURRENCY);
         msg.accumulatedAmount += amount;
         msg.touch();
         return msg.accumulatedAmount;
      }
   }

   public static void pushNoXp(Player player, String renderedMessage) {
      if (player != null && renderedMessage != null) {
         MessageThrottler.PendingMessage msg = getOrCreate(player, "no_xp", MessageThrottler.PolicyType.NO_XP);
         msg.count++;
         msg.lastMessage = renderedMessage;
         msg.touch();
      }
   }

   public static void pushQuestProgress(Player player, Objective objective, String renderedMessage) {
      if (player != null && objective != null && renderedMessage != null) {
         String key = "quest:" + System.identityHashCode(objective);
         MessageThrottler.PendingMessage msg = getOrCreate(player, key, MessageThrottler.PolicyType.QUEST);
         msg.lastMessage = renderedMessage;
         msg.touch();
      }
   }

   private static MessageThrottler.PendingMessage getOrCreate(Player player, String key, MessageThrottler.PolicyType policy) {
      return pending.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).computeIfAbsent(key, k -> new MessageThrottler.PendingMessage(policy));
   }

   private static void tick() {
      if (!pending.isEmpty()) {
         long now = System.currentTimeMillis();
         long quietMs = Math.max(1000L, DefaultConfig.getNotificationThrottleSeconds() * 1000L);
         Iterator<Entry<UUID, Map<String, MessageThrottler.PendingMessage>>> playerIt = pending.entrySet().iterator();

         while (playerIt.hasNext()) {
            Entry<UUID, Map<String, MessageThrottler.PendingMessage>> playerEntry = playerIt.next();
            Player player = Bukkit.getPlayer(playerEntry.getKey());
            if (player != null && player.isOnline()) {
               Iterator<MessageThrottler.PendingMessage> msgIt = playerEntry.getValue().values().iterator();

               while (msgIt.hasNext()) {
                  MessageThrottler.PendingMessage msg = msgIt.next();
                  if (now - msg.lastUpdatedMs >= quietMs) {
                     msg.flush(player);
                     msgIt.remove();
                  }
               }

               if (playerEntry.getValue().isEmpty()) {
                  playerIt.remove();
               }
            } else {
               playerIt.remove();
            }
         }
      }
   }

   private static final class PendingMessage {
      final MessageThrottler.PolicyType policy;
      long lastUpdatedMs = System.currentTimeMillis();
      double accumulatedAmount;
      int count;
      String lastMessage;

      PendingMessage(MessageThrottler.PolicyType policy) {
         this.policy = policy;
      }

      void touch() {
         this.lastUpdatedMs = System.currentTimeMillis();
      }

      void flush(Player player) {
         switch (this.policy) {
            case CURRENCY:
               player.sendMessage(
                  EconomySettingsConfig.getChatCurrencyShowerMessage()
                     .replace("$currency_name", EconomySettingsConfig.getCurrencyName())
                     .replace("$amount", Round.twoDecimalPlaces(this.accumulatedAmount) + "")
               );
               ActionBarCompositor.show(player, ActionBarCompositor.Source.ECONOMY, EconomySettingsConfig.getAdventurersGuildNotificationMessage());
               break;
            case NO_XP:
               String suffix = this.count > 1 ? " (x" + this.count + ")" : "";
               player.sendMessage(this.lastMessage + suffix);
               break;
            case QUEST:
               player.sendMessage(this.lastMessage);
         }
      }
   }

   private static enum PolicyType {
      CURRENCY,
      NO_XP,
      QUEST;
   }
}

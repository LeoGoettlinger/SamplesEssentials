package com.skyblockexp.ezrtp.teleport.queue;

import com.skyblockexp.ezrtp.config.RandomTeleportSettings;
import com.skyblockexp.ezrtp.config.network.TeleportQueueSettings;
import com.skyblockexp.ezrtp.message.MessageKey;
import com.skyblockexp.ezrtp.message.MessageProvider;
import com.skyblockexp.ezrtp.platform.PlatformScheduler;
import com.skyblockexp.ezrtp.pvptag.PvpTagService;
import com.skyblockexp.ezrtp.teleport.TeleportReason;
import com.skyblockexp.ezrtp.util.MessageUtil;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeleportQueueManager {
   private final JavaPlugin plugin;
   private final PlatformScheduler scheduler;
   private final MessageProvider messageProvider;
   private final Deque<TeleportQueueManager.QueuedTeleport> teleportQueue = new ArrayDeque<>();
   private TeleportQueueSettings queueSettings;
   private boolean queueProcessing;
   private UUID activeTeleportPlayer;
   private TeleportQueueManager.QueueExecutionHandler executionHandler;
   private PvpTagService pvpTagService;

   public TeleportQueueManager(JavaPlugin plugin, PlatformScheduler scheduler, TeleportQueueSettings queueSettings, MessageProvider messageProvider) {
      this.plugin = plugin;
      this.scheduler = scheduler;
      this.queueSettings = queueSettings != null ? queueSettings : TeleportQueueSettings.disabled();
      this.messageProvider = messageProvider;
   }

   public void setExecutionHandler(TeleportQueueManager.QueueExecutionHandler executionHandler) {
      this.executionHandler = executionHandler;
   }

   public void setPvpTagService(PvpTagService pvpTagService) {
      this.pvpTagService = pvpTagService;
   }

   public boolean enqueueIfNeeded(Player player, RandomTeleportSettings teleportSettings, TeleportReason reason) {
      if (this.queueSettings != null && this.queueSettings.isEnabled() && !this.queueSettings.canBypass(player)) {
         UUID playerId = player.getUniqueId();
         if (this.activeTeleportPlayer != null && this.activeTeleportPlayer.equals(playerId)) {
            if (!teleportSettings.isSuppressPlayerMessages()) {
               MessageUtil.send(player, this.messageProvider.format(MessageKey.TELEPORTING, player));
            }

            return true;
         } else {
            int existingPosition = this.findQueuePosition(playerId);
            if (existingPosition >= 0) {
               if (!teleportSettings.isSuppressPlayerMessages()) {
                  MessageUtil.send(
                     player, this.messageProvider.format(MessageKey.QUEUE_QUEUED, Map.of("position", String.valueOf(existingPosition + 1)), player)
                  );
               }

               return true;
            } else if (this.queueSettings.getMaxSize() > 0 && this.teleportQueue.size() >= this.queueSettings.getMaxSize()) {
               if (!teleportSettings.isSuppressPlayerMessages()) {
                  MessageUtil.send(player, this.messageProvider.format(MessageKey.QUEUE_FULL, player));
               }

               return true;
            } else {
               this.teleportQueue.addLast(new TeleportQueueManager.QueuedTeleport(playerId, teleportSettings, reason));
               if (!teleportSettings.isSuppressPlayerMessages()) {
                  MessageUtil.send(
                     player, this.messageProvider.format(MessageKey.QUEUE_QUEUED, Map.of("position", String.valueOf(this.teleportQueue.size())), player)
                  );
               }

               if (!this.queueProcessing) {
                  this.queueProcessing = true;
                  this.scheduleDispatch(Math.max(0L, this.queueSettings.getStartDelayTicks()));
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   private void scheduleDispatch(long delayTicks) {
      try {
         this.scheduler.executeGlobalDelayed(this::dispatchNextTeleport, delayTicks);
      } catch (IllegalStateException var4) {
         this.queueProcessing = false;
         this.teleportQueue.clear();
      }
   }

   private void dispatchNextTeleport() {
      if (this.executionHandler == null) {
         this.queueProcessing = false;
      } else {
         TeleportQueueManager.QueuedTeleport next;
         while ((next = this.teleportQueue.pollFirst()) != null) {
            Player player = this.plugin.getServer().getPlayer(next.playerId());
            if (player != null && player.isOnline()) {
               if (this.pvpTagService == null
                  || !next.settings().getPvpTagIntegrationSettings().isCancelQueuedOnPvpTag()
                  || !this.pvpTagService.isInCombat(player)) {
                  this.activeTeleportPlayer = next.playerId();
                  this.executionHandler.execute(player, next.settings(), next.reason(), this::completeQueuedTeleport);
                  return;
               }

               if (!next.settings().isSuppressPlayerMessages()) {
                  MessageUtil.send(player, this.messageProvider.format(MessageKey.QUEUE_PVP_TAG_CANCEL, player));
               }
            }
         }

         this.queueProcessing = false;
         this.activeTeleportPlayer = null;
      }
   }

   private void completeQueuedTeleport() {
      this.activeTeleportPlayer = null;
      if (this.teleportQueue.isEmpty()) {
         this.queueProcessing = false;
      } else {
         long delay = this.queueSettings != null ? Math.max(0L, this.queueSettings.getIntervalTicks()) : 0L;
         this.scheduleDispatch(delay);
      }
   }

   private int findQueuePosition(UUID playerId) {
      int index = 0;

      for (TeleportQueueManager.QueuedTeleport queued : this.teleportQueue) {
         if (queued.playerId().equals(playerId)) {
            return index;
         }

         index++;
      }

      return -1;
   }

   public void reload(TeleportQueueSettings newQueueSettings) {
      this.queueSettings = newQueueSettings != null ? newQueueSettings : TeleportQueueSettings.disabled();
      if (!this.queueSettings.isEnabled() && this.teleportQueue.isEmpty()) {
         this.queueProcessing = false;
         this.activeTeleportPlayer = null;
      }
   }

   public void shutdown() {
      this.teleportQueue.clear();
      this.queueProcessing = false;
      this.activeTeleportPlayer = null;
   }

   @FunctionalInterface
   public interface QueueExecutionHandler {
      void execute(Player var1, RandomTeleportSettings var2, TeleportReason var3, Runnable var4);
   }

   public record QueuedTeleport(UUID playerId, RandomTeleportSettings settings, TeleportReason reason) {
   }
}

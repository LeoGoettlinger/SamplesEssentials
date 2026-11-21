package net.essentialsx.api.v2.events;

import com.earth2me.essentials.AsyncTeleport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class TeleportWarmupCancelledEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private final AsyncTeleport.TeleportType teleportType;
   private final TeleportWarmupCancelledEvent.CancelReason cancelReason;
   private final boolean notifyUser;

   public TeleportWarmupCancelledEvent(
      final Player player,
      final AsyncTeleport.TeleportType teleportType,
      final TeleportWarmupCancelledEvent.CancelReason cancelReason,
      final boolean notifyUser
   ) {
      super(!Bukkit.isPrimaryThread());
      this.player = player;
      this.teleportType = teleportType;
      this.cancelReason = cancelReason;
      this.notifyUser = notifyUser;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public Player getPlayer() {
      return this.player;
   }

   public AsyncTeleport.TeleportType getTeleportType() {
      return this.teleportType;
   }

   public TeleportWarmupCancelledEvent.CancelReason getCancelReason() {
      return this.cancelReason;
   }

   public boolean isPlayerNotified() {
      return this.notifyUser;
   }

   public static enum CancelReason {
      LEAVE,
      MOVE;
   }
}

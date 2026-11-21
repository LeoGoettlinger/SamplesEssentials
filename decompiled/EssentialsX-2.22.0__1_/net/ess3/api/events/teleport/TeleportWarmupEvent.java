package net.ess3.api.events.teleport;

import com.earth2me.essentials.ITarget;
import net.ess3.api.IUser;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class TeleportWarmupEvent extends TeleportEvent {
   private static final HandlerList handlers = new HandlerList();
   private double delay;

   public TeleportWarmupEvent(final IUser teleporter, final IUser teleportee, final TeleportCause cause, final ITarget target, final double delay) {
      super(teleporter, teleportee, cause, target);
      this.delay = delay;
   }

   public TeleportWarmupEvent(final IUser teleportee, final TeleportCause cause, final ITarget target, final double delay) {
      super(teleportee, cause, target);
      this.delay = delay;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public double getDelay() {
      return this.delay;
   }

   public void setDelay(final double delay) {
      this.delay = delay;
   }
}

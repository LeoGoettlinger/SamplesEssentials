package net.ess3.api.events.teleport;

import com.earth2me.essentials.ITarget;
import net.ess3.api.IUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public abstract class TeleportEvent extends Event implements Cancellable {
   private final IUser teleporter;
   private final IUser teleportee;
   private final TeleportCause cause;
   private final ITarget target;
   private boolean cancelled = false;

   TeleportEvent(final IUser teleporter, final IUser teleportee, final TeleportCause cause, final ITarget target) {
      super(!Bukkit.isPrimaryThread());
      this.teleporter = teleporter;
      this.teleportee = teleportee;
      this.cause = cause;
      this.target = target;
   }

   TeleportEvent(final IUser teleportee, final TeleportCause cause, final ITarget target) {
      this(teleportee, teleportee, cause, target);
   }

   public IUser getTeleporter() {
      return this.teleporter;
   }

   public IUser getTeleportee() {
      return this.teleportee;
   }

   public TeleportCause getTeleportCause() {
      return this.cause;
   }

   public ITarget getTarget() {
      return this.target;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(final boolean b) {
      this.cancelled = b;
   }
}

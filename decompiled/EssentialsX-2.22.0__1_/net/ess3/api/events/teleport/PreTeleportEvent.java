package net.ess3.api.events.teleport;

import com.earth2me.essentials.ITarget;
import net.ess3.api.IUser;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class PreTeleportEvent extends TeleportEvent {
   private static final HandlerList handlers = new HandlerList();

   public PreTeleportEvent(final IUser teleportee, final TeleportCause cause, final ITarget target) {
      super(teleportee, cause, target);
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}

package net.ess3.api.events;

import com.earth2me.essentials.Kit;
import net.ess3.api.IUser;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class KitClaimEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Kit kit;
   private final IUser user;
   private boolean cancelled;

   public KitClaimEvent(final IUser user, final Kit kit) {
      super(!Bukkit.getServer().isPrimaryThread());
      this.user = user;
      this.kit = kit;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public IUser getUser() {
      return this.user;
   }

   public Kit getKit() {
      return this.kit;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(final boolean cancel) {
      this.cancelled = cancel;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}

package net.essentialsx.api.v2.events;

import net.ess3.api.IUser;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WarpModifyEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IUser user;
   private final String warpName;
   private final Location oldLocation;
   private final Location newLocation;
   private final WarpModifyEvent.WarpModifyCause cause;
   private boolean cancelled;

   public WarpModifyEvent(IUser user, String warpName, Location oldLocation, Location newLocation, WarpModifyEvent.WarpModifyCause cause) {
      this.user = user;
      this.warpName = warpName;
      this.oldLocation = oldLocation;
      this.newLocation = newLocation;
      this.cause = cause;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   public IUser getUser() {
      return this.user;
   }

   public WarpModifyEvent.WarpModifyCause getCause() {
      return this.cause;
   }

   public String getWarpName() {
      return this.warpName;
   }

   public Location getOldLocation() {
      return this.oldLocation;
   }

   public Location getNewLocation() {
      return this.newLocation;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public static enum WarpModifyCause {
      UPDATE,
      CREATE,
      DELETE;
   }
}

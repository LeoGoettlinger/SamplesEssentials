package net.coreprotect.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CoreProtectPreLogEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private boolean cancelled = false;
   private String user;
   private Location location;

   public CoreProtectPreLogEvent(String user, Location location) {
      super(true);
      this.user = user;
      this.location = location;
   }

   @Deprecated
   public CoreProtectPreLogEvent(String user) {
      super(true);
      this.user = user;
      this.location = null;
   }

   public String getUser() {
      return this.user;
   }

   public Location getLocation() {
      return this.location;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   public void setUser(String newUser) {
      if (newUser != null && !newUser.isEmpty()) {
         this.user = newUser;
      } else {
         throw new IllegalArgumentException("Invalid user");
      }
   }

   public void setLocation(Location newLocation) {
      if (newLocation == null) {
         throw new IllegalArgumentException("Invalid location");
      } else {
         this.location = newLocation;
      }
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}

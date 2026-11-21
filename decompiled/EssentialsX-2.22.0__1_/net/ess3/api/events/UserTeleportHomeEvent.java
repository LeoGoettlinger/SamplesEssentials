package net.ess3.api.events;

import net.ess3.api.IUser;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserTeleportHomeEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IUser user;
   private final String homeName;
   private final Location target;
   private final UserTeleportHomeEvent.HomeType homeType;
   private boolean cancelled = false;

   public UserTeleportHomeEvent(final IUser user, final String homeName, final Location target, final UserTeleportHomeEvent.HomeType homeType) {
      this.user = user;
      this.homeName = homeName;
      this.target = target;
      this.homeType = homeType;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public IUser getUser() {
      return this.user;
   }

   public String getHomeName() {
      return this.homeName;
   }

   public Location getHomeLocation() {
      return this.target;
   }

   public UserTeleportHomeEvent.HomeType getHomeType() {
      return this.homeType;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(final boolean cancelled) {
      this.cancelled = cancelled;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static enum HomeType {
      HOME,
      BED,
      SPAWN;
   }
}

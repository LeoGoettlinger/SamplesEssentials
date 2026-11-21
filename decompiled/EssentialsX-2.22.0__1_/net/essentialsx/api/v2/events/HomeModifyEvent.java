package net.essentialsx.api.v2.events;

import net.ess3.api.IUser;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HomeModifyEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IUser user;
   private final IUser homeOwner;
   private final Location newLocation;
   private final Location oldLocation;
   private final String newName;
   private final String oldName;
   private final HomeModifyEvent.HomeModifyCause cause;
   private boolean canceled = false;

   public HomeModifyEvent(IUser user, IUser homeOwner, String name, Location location, boolean create) {
      this(
         user,
         homeOwner,
         create ? location : null,
         create ? null : location,
         create ? name : null,
         create ? null : name,
         create ? HomeModifyEvent.HomeModifyCause.CREATE : HomeModifyEvent.HomeModifyCause.DELETE
      );
   }

   public HomeModifyEvent(IUser user, IUser homeOwner, String oldName, String newName, Location location) {
      this(user, homeOwner, location, location, newName, oldName, HomeModifyEvent.HomeModifyCause.RENAME);
   }

   public HomeModifyEvent(IUser user, IUser homeOwner, String name, Location oldLocation, Location newLocation) {
      this(user, homeOwner, newLocation, oldLocation, name, name, HomeModifyEvent.HomeModifyCause.UPDATE);
   }

   public HomeModifyEvent(
      IUser user, IUser homeOwner, Location newLocation, Location oldLocation, String newName, String oldName, HomeModifyEvent.HomeModifyCause cause
   ) {
      this.user = user;
      this.homeOwner = homeOwner;
      this.newLocation = newLocation;
      this.oldLocation = oldLocation;
      this.newName = newName;
      this.oldName = oldName;
      this.cause = cause;
   }

   public IUser getUser() {
      return this.user;
   }

   public IUser getHomeOwner() {
      return this.homeOwner;
   }

   public Location getNewLocation() {
      return this.newLocation;
   }

   public Location getOldLocation() {
      return this.oldLocation;
   }

   public String getNewName() {
      return this.newName;
   }

   public String getOldName() {
      return this.oldName;
   }

   public HomeModifyEvent.HomeModifyCause getCause() {
      return this.cause;
   }

   public void setCancelled(boolean cancel) {
      this.canceled = cancel;
   }

   public boolean isCancelled() {
      return this.canceled;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public static enum HomeModifyCause {
      CREATE,
      DELETE,
      RENAME,
      UPDATE;
   }
}

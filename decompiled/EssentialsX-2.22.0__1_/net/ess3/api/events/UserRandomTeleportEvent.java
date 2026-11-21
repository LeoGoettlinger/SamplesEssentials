package net.ess3.api.events;

import net.ess3.api.IUser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class UserRandomTeleportEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final IUser user;
   private String name;
   private Location center;
   private double minRange;
   private double maxRange;
   private boolean cancelled = false;
   private boolean modified = false;

   public UserRandomTeleportEvent(final IUser user, final String name, final Location center, final double minRange, final double maxRange) {
      super(!Bukkit.isPrimaryThread());
      this.user = user;
      this.name = name;
      this.center = center;
      this.minRange = minRange;
      this.maxRange = maxRange;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public IUser getUser() {
      return this.user;
   }

   public String getName() {
      return this.name;
   }

   public Location getCenter() {
      return this.center;
   }

   public void setCenter(final Location center) {
      if (!this.center.equals(center)) {
         this.modified = true;
      }

      this.center = center;
   }

   public double getMinRange() {
      return this.minRange;
   }

   public void setMinRange(final double minRange) {
      if (this.minRange != minRange) {
         this.modified = true;
      }

      this.minRange = minRange;
   }

   public double getMaxRange() {
      return this.maxRange;
   }

   public void setMaxRange(final double maxRange) {
      if (this.maxRange != maxRange) {
         this.modified = true;
      }

      this.maxRange = maxRange;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(final boolean b) {
      this.cancelled = b;
   }

   public boolean isModified() {
      return this.modified;
   }

   public HandlerList getHandlers() {
      return handlers;
   }
}

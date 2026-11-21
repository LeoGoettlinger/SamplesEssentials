package com.magmaguy.elitemobs.api;

import lombok.Generated;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class EliteDamageEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Event event;
   private boolean isCancelled = false;
   private double damage;

   public EliteDamageEvent(double damage, Event event) {
      this.damage = damage;
      this.event = event;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public void setDamage(double damage) {
      this.damage = damage;
   }

   public boolean isCancelled() {
      return this.isCancelled;
   }

   public void setCancelled(boolean b) {
      this.isCancelled = b;
      ((Cancellable)this.event).setCancelled(b);
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public double getDamage() {
      return this.damage;
   }
}

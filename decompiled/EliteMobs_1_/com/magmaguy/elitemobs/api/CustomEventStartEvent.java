package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.events.CustomEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CustomEventStartEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private CustomEvent customEvent;
   private boolean cancelled = false;

   public CustomEventStartEvent(CustomEvent customEvent) {
      this.customEvent = customEvent;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public CustomEvent getCustomEvent() {
      return this.customEvent;
   }

   public void setCustomEvent(CustomEvent customEvent) {
      this.customEvent = customEvent;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}

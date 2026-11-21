package com.magmaguy.elitemobs.api.instanced;

import com.magmaguy.elitemobs.instanced.MatchInstance;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MatchInstantiateEvent extends Event implements MatchEvent, Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final MatchInstance matchInstance;
   private boolean cancelled = false;

   public MatchInstantiateEvent(MatchInstance matchInstance) {
      this.matchInstance = matchInstance;
   }

   @Override
   public MatchInstance getInstance() {
      return this.matchInstance;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean b) {
      this.cancelled = b;
   }
}

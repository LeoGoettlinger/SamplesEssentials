package com.magmaguy.elitemobs.api.instanced;

import com.magmaguy.elitemobs.instanced.MatchInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class MatchJoinEvent extends Event implements MatchEvent, MatchPlayerEvent, Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final MatchInstance matchInstance;
   private final Player player;
   private boolean cancelled = false;

   public MatchJoinEvent(MatchInstance matchInstance, Player player) {
      this.matchInstance = matchInstance;
      this.player = player;
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

   @Override
   public Player getPlayer() {
      return this.player;
   }
}

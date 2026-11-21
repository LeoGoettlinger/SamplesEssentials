package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.arena.ArenaInstance;
import lombok.Generated;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ArenaStartEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final ArenaInstance arenaInstance;

   public ArenaStartEvent(ArenaInstance arenaInstance) {
      this.arenaInstance = arenaInstance;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public ArenaInstance getArenaInstance() {
      return this.arenaInstance;
   }
}

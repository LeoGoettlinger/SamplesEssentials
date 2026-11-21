package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import lombok.Generated;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class DungeonStartEvent extends Event {
   private static final HandlerList handlers = new HandlerList();
   private final DungeonInstance dungeonInstance;

   public DungeonStartEvent(DungeonInstance dungeonInstance) {
      this.dungeonInstance = dungeonInstance;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   @Generated
   public DungeonInstance getDungeonInstance() {
      return this.dungeonInstance;
   }
}

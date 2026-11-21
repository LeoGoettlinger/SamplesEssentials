package org.skriptlang.skript.bukkit.loottables;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LootContextCreateEvent extends Event {
   private final LootContextWrapper contextWrapper;

   public LootContextCreateEvent(LootContextWrapper context) {
      this.contextWrapper = context;
   }

   public LootContextWrapper getContextWrapper() {
      return this.contextWrapper;
   }

   public HandlerList getHandlers() {
      throw new UnsupportedOperationException();
   }
}

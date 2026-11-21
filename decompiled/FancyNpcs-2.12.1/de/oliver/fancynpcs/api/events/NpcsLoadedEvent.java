package de.oliver.fancynpcs.api.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public class NpcsLoadedEvent extends Event {
   private static final HandlerList handlerList = new HandlerList();

   public static HandlerList getHandlerList() {
      return handlerList;
   }

   public HandlerList getHandlers() {
      return handlerList;
   }
}

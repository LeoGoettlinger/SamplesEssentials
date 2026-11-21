package ch.njol.skript.events.bukkit;

import org.bukkit.event.HandlerList;

public class ScheduledNoWorldEvent extends ScheduledEvent {
   private static final HandlerList handlers = new HandlerList();

   public ScheduledNoWorldEvent() {
      super(null);
   }

   @Override
   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}

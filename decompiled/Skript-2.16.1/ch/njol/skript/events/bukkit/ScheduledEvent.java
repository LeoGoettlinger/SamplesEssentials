package ch.njol.skript.events.bukkit;

import ch.njol.skript.registrations.EventValues;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class ScheduledEvent extends Event {
   @Nullable
   private final World world;
   private static final HandlerList handlers = new HandlerList();

   public ScheduledEvent(@Nullable World world) {
      this.world = world;
   }

   @Nullable
   public final World getWorld() {
      return this.world;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   static {
      EventValues.registerEventValue(
         ScheduledEvent.class,
         World.class,
         ScheduledEvent::getWorld,
         EventValues.TIME_NOW,
         "There's no world in a periodic event if no world is given in the event (e.g. like 'every hour in \"world\"')",
         ScheduledNoWorldEvent.class
      );
   }
}

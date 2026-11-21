package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GraveTimeoutEvent extends GraveEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   public GraveTimeoutEvent(@NotNull Grave grave) {
      super(grave, grave.getLocationDeath(), null, null);
   }

   @NotNull
   @Override
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}

package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GraveCompassUseEvent extends GravePlayerEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   public GraveCompassUseEvent(@NotNull Player player, @NotNull Grave grave) {
      super(grave, player, grave.getLocationDeath(), null, null, null, player, null);
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

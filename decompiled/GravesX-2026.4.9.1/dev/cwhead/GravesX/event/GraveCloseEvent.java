package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GravePlayerEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class GraveCloseEvent extends GravePlayerEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   public GraveCloseEvent(@NotNull InventoryView inventoryView, @NotNull Grave grave, @NotNull Player player) {
      super(grave, player, grave.getLocationDeath(), null, null, inventoryView, player, null);
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

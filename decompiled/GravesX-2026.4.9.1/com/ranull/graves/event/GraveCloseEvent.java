package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.exception.GravesXIllegalArgumentException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

@Deprecated(
   since = "4.9.9.1",
   forRemoval = true
)
@ScheduledForRemoval(
   inVersion = "2027.4.9.1"
)
public class GraveCloseEvent extends dev.cwhead.GravesX.event.GraveCloseEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveCloseEvent(@NotNull InventoryView inventoryView, @NotNull Grave grave, @NotNull Player player) {
      super(inventoryView, grave, player);
   }

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveCloseEvent(@NotNull Grave grave, @NotNull InventoryView inventoryView, @NotNull Entity entity) {
      super(inventoryView, grave, requirePlayer(entity));
   }

   @NotNull
   private static Player requirePlayer(@NotNull Entity entity) {
      if (entity instanceof Player p) {
         return p;
      } else {
         throw new GravesXIllegalArgumentException("GraveCloseEvent requires a Player. Received " + entity.getType() + " instead");
      }
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

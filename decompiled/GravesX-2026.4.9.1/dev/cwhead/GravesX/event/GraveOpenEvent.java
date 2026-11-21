package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;

public class GraveOpenEvent extends GraveEntityEvent implements Cancellable {
   private static final HandlerList HANDLERS = new HandlerList();
   @NotNull
   private final InventoryView view;
   private boolean cancelled;

   public GraveOpenEvent(@NotNull InventoryView view, @NotNull Grave grave, @NotNull Player player) {
      super(grave, player, grave.getLocationDeath(), null, null, null, null);
      this.view = view;
      this.cancelled = false;
   }

   @NotNull
   public InventoryView getView() {
      return this.view;
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
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

   @NotNull
   @Override
   public Player getPlayer() {
      return (Player)this.getEntity();
   }
}

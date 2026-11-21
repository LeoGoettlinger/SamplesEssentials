package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GraveItemTakeEvent extends GraveEntityEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   @NotNull
   private final ItemStack item;
   @NotNull
   private final InventoryAction action;

   public GraveItemTakeEvent(@NotNull Grave grave, @NotNull Player player, @NotNull ItemStack item, @NotNull InventoryAction action) {
      super(grave, player, grave.getLocationDeath(), null, null, player, null);
      this.item = item;
      this.action = action;
   }

   @NotNull
   public ItemStack getItem() {
      return this.item;
   }

   @NotNull
   public InventoryAction getAction() {
      return this.action;
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

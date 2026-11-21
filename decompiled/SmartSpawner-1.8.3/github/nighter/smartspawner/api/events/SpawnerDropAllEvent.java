package github.nighter.smartspawner.api.events;

import java.util.List;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SpawnerDropAllEvent extends Event implements Cancellable {
   private final Player player;
   private final Location location;
   private final List<ItemStack> items;
   private boolean cancelled = false;
   private static final HandlerList handlers = new HandlerList();

   public SpawnerDropAllEvent(Player player, Location location, List<ItemStack> items) {
      this.player = player;
      this.location = location;
      this.items = items;
   }

   @NotNull
   public HandlerList getHandlers() {
      return handlers;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return handlers;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public Location getLocation() {
      return this.location;
   }

   @Generated
   public List<ItemStack> getItems() {
      return this.items;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}

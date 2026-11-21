package github.nighter.smartspawner.api.events;

import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpawnerPlayerBreakEvent extends SpawnerBreakEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final Player player;
   private boolean cancelled = false;

   public SpawnerPlayerBreakEvent(Player player, Location location, int quantity) {
      this(player, location, quantity, null);
   }

   public SpawnerPlayerBreakEvent(Player player, Location location, int quantity, @Nullable EntityType entityType) {
      super(player, location, quantity, entityType);
      this.player = player;
   }

   @NotNull
   @Override
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
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Generated
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}

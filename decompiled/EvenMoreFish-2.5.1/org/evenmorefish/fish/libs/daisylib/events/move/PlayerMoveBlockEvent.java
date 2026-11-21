package org.evenmorefish.fish.libs.daisylib.events.move;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public class PlayerMoveBlockEvent extends AbstractMoveEvent {
   private static final HandlerList handlers = new HandlerList();
   private final Block to;
   private final Block from;

   public PlayerMoveBlockEvent(@NonNull Player player, @NonNull Location to, @NonNull Location from) {
      super(player, to, from);
      this.to = to.getBlock();
      this.from = from.getBlock();
   }

   @NonNull
   public Block getTo() {
      return this.to;
   }

   @NonNull
   public Block getFrom() {
      return this.from;
   }

   @NonNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}

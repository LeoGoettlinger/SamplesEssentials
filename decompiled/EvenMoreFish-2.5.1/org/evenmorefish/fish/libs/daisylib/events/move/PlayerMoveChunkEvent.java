package org.evenmorefish.fish.libs.daisylib.events.move;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.NonNull;

public class PlayerMoveChunkEvent extends AbstractMoveEvent {
   private static final HandlerList handlers = new HandlerList();
   private final Chunk to;
   private final Chunk from;

   public PlayerMoveChunkEvent(@NonNull Player player, @NonNull Location to, @NonNull Location from) {
      super(player, to, from);
      this.to = to.getChunk();
      this.from = from.getChunk();
   }

   @NonNull
   public Chunk getTo() {
      return this.to;
   }

   @NonNull
   public Chunk getFrom() {
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

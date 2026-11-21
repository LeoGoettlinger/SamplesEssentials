package org.evenmorefish.fish.libs.daisylib.events.brush;

import org.bukkit.block.Block;
import org.bukkit.block.BrushableBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.evenmorefish.fish.libs.daisylib.blockstate.CustomBrushable;
import org.jspecify.annotations.NonNull;

public class BlockBrushEvent extends PlayerEvent implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final CustomBrushable customBrushable;
   private boolean cancelled = false;

   public BlockBrushEvent(@NonNull Player player, @NonNull BrushableBlock brushable) {
      super(player);
      this.customBrushable = CustomBrushable.customBrushable(brushable);
   }

   @NonNull
   public BrushableBlock getState() {
      return this.customBrushable.getState();
   }

   @NonNull
   public Block getBlock() {
      return this.customBrushable.getBlock();
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   @NonNull
   public HandlerList getHandlers() {
      return handlers;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }
}

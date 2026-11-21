package net.thenextlvl.protect.area.event.player;

import net.thenextlvl.protect.area.Area;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class PlayerAreaTransitionEvent extends PlayerAreaEvent implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   private final Area previous;
   private boolean cancelled;

   @Internal
   public PlayerAreaTransitionEvent(Player player, Area from, Area to) {
      super(player, to);
      this.previous = from;
   }

   @Contract(
      pure = true
   )
   public Area getPrevious() {
      return this.previous;
   }

   @Contract(
      pure = true
   )
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Contract(
      mutates = "this"
   )
   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   @Contract(
      pure = true
   )
   public HandlerList getHandlers() {
      return handlerList;
   }

   @Contract(
      pure = true
   )
   public static HandlerList getHandlerList() {
      return handlerList;
   }
}

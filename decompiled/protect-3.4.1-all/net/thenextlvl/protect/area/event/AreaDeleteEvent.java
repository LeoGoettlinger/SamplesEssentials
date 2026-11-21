package net.thenextlvl.protect.area.event;

import net.thenextlvl.protect.area.RegionizedArea;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class AreaDeleteEvent extends AreaEvent<RegionizedArea<?>> implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   private boolean cancelled;

   @Internal
   public AreaDeleteEvent(RegionizedArea<?> area) {
      super(area);
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

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

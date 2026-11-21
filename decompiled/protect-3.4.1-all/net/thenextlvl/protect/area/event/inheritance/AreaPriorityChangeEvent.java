package net.thenextlvl.protect.area.event.inheritance;

import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.event.AreaEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class AreaPriorityChangeEvent extends AreaEvent<Area> implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   private boolean cancelled;
   private int priority;

   @Internal
   public AreaPriorityChangeEvent(Area area, int priority) {
      super(area);
      this.priority = priority;
   }

   @Contract(
      pure = true
   )
   public int getPriority() {
      return this.priority;
   }

   @Contract(
      mutates = "this"
   )
   public void setPriority(int priority) {
      this.priority = priority;
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

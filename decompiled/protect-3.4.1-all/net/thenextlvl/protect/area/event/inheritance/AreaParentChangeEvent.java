package net.thenextlvl.protect.area.event.inheritance;

import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.event.AreaEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.Nullable;

public final class AreaParentChangeEvent extends AreaEvent<Area> implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   private boolean cancelled;
   @Nullable
   private Area parent;

   @Internal
   public AreaParentChangeEvent(Area area, @Nullable Area parent) {
      super(area);
      this.parent = parent;
   }

   @Contract(
      pure = true
   )
   @Nullable
   public Area getParent() {
      return this.parent;
   }

   @Contract(
      mutates = "this"
   )
   public void setParent(@Nullable Area parent) {
      this.parent = parent;
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

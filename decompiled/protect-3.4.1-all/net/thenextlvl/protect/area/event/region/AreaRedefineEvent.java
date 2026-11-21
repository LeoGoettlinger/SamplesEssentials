package net.thenextlvl.protect.area.event.region;

import com.sk89q.worldedit.regions.Region;
import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.area.event.AreaEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class AreaRedefineEvent<T extends Region> extends AreaEvent<RegionizedArea<T>> implements Cancellable {
   private static final HandlerList handlerList = new HandlerList();
   private boolean cancelled;
   private T region;

   @Internal
   public AreaRedefineEvent(RegionizedArea<T> area, T region) {
      super(area);
      this.region = region;
   }

   @Contract(
      pure = true
   )
   public T getRegion() {
      return this.region;
   }

   @Contract(
      mutates = "this"
   )
   public void setRegion(T region) {
      this.region = region;
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

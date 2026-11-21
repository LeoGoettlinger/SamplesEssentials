package net.thenextlvl.protect.area.event.flag;

import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.flag.Flag;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NonNull;

public final class AreaFlagChangeEvent<T> extends AreaFlagEvent<T> {
   @NonNull
   private static final HandlerList handlerList = new HandlerList();
   private T newState;

   @Internal
   public AreaFlagChangeEvent(@NonNull Area area, @NonNull Flag<T> flag, T newState) {
      super(area, flag);
      this.newState = newState;
   }

   @Contract(
      pure = true
   )
   public T getNewState() {
      return this.newState;
   }

   @Contract(
      mutates = "this"
   )
   public void setNewState(T newState) {
      this.newState = newState;
   }

   @Contract(
      pure = true
   )
   @NonNull
   public HandlerList getHandlers() {
      return handlerList;
   }

   @Contract(
      pure = true
   )
   @NonNull
   public static HandlerList getHandlerList() {
      return handlerList;
   }
}

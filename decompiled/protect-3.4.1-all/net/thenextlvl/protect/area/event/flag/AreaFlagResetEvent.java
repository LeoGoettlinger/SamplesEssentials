package net.thenextlvl.protect.area.event.flag;

import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.flag.Flag;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NonNull;

public final class AreaFlagResetEvent<T> extends AreaFlagEvent<T> {
   @NonNull
   private static final HandlerList handlerList = new HandlerList();

   @Internal
   public AreaFlagResetEvent(@NonNull Area area, @NonNull Flag<T> flag) {
      super(area, flag);
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

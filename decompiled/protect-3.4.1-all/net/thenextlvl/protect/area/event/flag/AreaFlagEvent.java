package net.thenextlvl.protect.area.event.flag;

import net.thenextlvl.protect.area.Area;
import net.thenextlvl.protect.area.event.AreaEvent;
import net.thenextlvl.protect.flag.Flag;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NonNull;

public abstract class AreaFlagEvent<T> extends AreaEvent<Area> implements Cancellable {
   @NonNull
   private final Flag<T> flag;
   private boolean cancelled;

   @Internal
   protected AreaFlagEvent(@NonNull Area area, @NonNull Flag<T> flag) {
      super(area);
      this.flag = flag;
   }

   @Contract(
      pure = true
   )
   @NonNull
   public Flag<T> getFlag() {
      return this.flag;
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
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }
}

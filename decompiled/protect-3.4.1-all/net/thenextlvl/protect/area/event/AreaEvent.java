package net.thenextlvl.protect.area.event;

import net.thenextlvl.protect.area.Area;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;

public abstract class AreaEvent<T extends Area> extends Event {
   private final T area;

   @Internal
   protected AreaEvent(T area) {
      this(!area.getServer().isPrimaryThread(), area);
   }

   @Internal
   protected AreaEvent(boolean async, T area) {
      super(async);
      this.area = area;
   }

   @Contract(
      pure = true
   )
   public T getArea() {
      return this.area;
   }
}

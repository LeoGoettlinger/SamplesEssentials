package net.thenextlvl.protect.area.event;

import net.thenextlvl.protect.area.Area;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class AreaLoadEvent extends AreaEvent<Area> {
   private static final HandlerList handlerList = new HandlerList();

   @Internal
   public AreaLoadEvent(Area area) {
      super(area);
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

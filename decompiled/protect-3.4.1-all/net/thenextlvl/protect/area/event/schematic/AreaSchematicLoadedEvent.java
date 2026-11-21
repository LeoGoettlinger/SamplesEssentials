package net.thenextlvl.protect.area.event.schematic;

import net.thenextlvl.protect.area.RegionizedArea;
import net.thenextlvl.protect.area.event.AreaEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.ApiStatus.Internal;

public final class AreaSchematicLoadedEvent extends AreaEvent<RegionizedArea<?>> {
   private static final HandlerList handlerList = new HandlerList();

   @Internal
   public AreaSchematicLoadedEvent(RegionizedArea<?> area) {
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

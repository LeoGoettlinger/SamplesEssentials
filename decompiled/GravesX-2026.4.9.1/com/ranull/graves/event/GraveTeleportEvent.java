package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

@Deprecated(
   since = "4.9.9.1",
   forRemoval = true
)
@ScheduledForRemoval(
   inVersion = "2027.4.9.1"
)
public class GraveTeleportEvent extends dev.cwhead.GravesX.event.GraveTeleportEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveTeleportEvent(@NotNull Grave grave, @NotNull Entity entity) {
      super(grave, entity);
   }

   @NotNull
   @Override
   public HandlerList getHandlers() {
      return HANDLERS;
   }

   @NotNull
   public static HandlerList getHandlerList() {
      return HANDLERS;
   }
}

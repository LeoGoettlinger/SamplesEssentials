package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.exception.GravesXIllegalArgumentException;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
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
public class GraveParticleEvent extends dev.cwhead.GravesX.event.GraveParticleEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveParticleEvent(@NotNull Player player, @NotNull Grave grave) {
      super(player, grave);
   }

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveParticleEvent(@NotNull Entity entity, @NotNull Grave grave) {
      super(requirePlayer(entity), grave);
   }

   @NotNull
   private static Player requirePlayer(@NotNull Entity entity) {
      if (entity instanceof Player p) {
         return p;
      } else {
         throw new GravesXIllegalArgumentException("GraveParticleEvent requires a Player. Received " + entity.getType() + " instead.");
      }
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

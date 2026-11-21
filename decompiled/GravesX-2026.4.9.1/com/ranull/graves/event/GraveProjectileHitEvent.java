package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;

@Deprecated(
   since = "4.9.9.1",
   forRemoval = true
)
@ScheduledForRemoval(
   inVersion = "2027.4.9.1"
)
public class GraveProjectileHitEvent extends dev.cwhead.GravesX.event.GraveProjectileHitEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveProjectileHitEvent(@NotNull Location location, @NotNull Player player, @NotNull Grave grave, @NotNull Entity entity, @Nullable Block block) {
      super(location, player, grave, entity, block);
   }

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveProjectileHitEvent(
      @NotNull Location location, @NotNull LivingEntity livingEntity, @NotNull Grave grave, @NotNull Entity entity, @Nullable Block block
   ) {
      super(location, livingEntity, grave, entity, block);
   }

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveProjectileHitEvent(@NotNull Location location, @NotNull Grave grave, @NotNull Entity entity, @Nullable Block block) {
      super(location, grave, entity, block);
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

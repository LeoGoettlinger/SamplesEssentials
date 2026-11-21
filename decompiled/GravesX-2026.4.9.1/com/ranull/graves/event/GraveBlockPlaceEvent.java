package com.ranull.graves.event;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
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
public class GraveBlockPlaceEvent extends dev.cwhead.GravesX.event.GraveBlockPlaceEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveBlockPlaceEvent(@NotNull Grave grave, @NotNull Location location, @NotNull BlockData.BlockType blockType, Block block, LivingEntity livingEntity) {
      super(grave, location, blockType, block, livingEntity);
   }

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveBlockPlaceEvent(@NotNull Grave grave, @NotNull Location location, @NotNull BlockData.BlockType blockType) {
      super(grave, location, blockType, null, null);
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

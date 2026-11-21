package com.ranull.graves.event;

import com.ranull.graves.type.Grave;
import java.util.Collection;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
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
public class GraveCreateEvent extends dev.cwhead.GravesX.event.GraveCreateEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   @Deprecated(
      since = "4.9.9.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveCreateEvent(@NotNull Entity entity, @NotNull Grave grave) {
      super(entity, grave);
   }

   @Deprecated(
      since = "4.9.14.1",
      forRemoval = true
   )
   @ScheduledForRemoval(
      inVersion = "2027.4.9.1"
   )
   public GraveCreateEvent(
      @NotNull Entity entity, @NotNull Grave grave, @Nullable Collection<ItemStack> ignoredItems, @Nullable Collection<Block> ignoredBlocks
   ) {
      super(entity, grave, ignoredItems, ignoredBlocks);
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

package dev.cwhead.GravesX.event;

import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraveProjectileHitEvent extends GraveEntityEvent {
   private static final HandlerList HANDLERS = new HandlerList();

   public GraveProjectileHitEvent(@NotNull Location location, @NotNull Player player, @NotNull Grave grave, @NotNull Entity entity, @Nullable Block block) {
      super(grave, entity, location, null, block, player, null);
   }

   public GraveProjectileHitEvent(
      @NotNull Location location, @NotNull LivingEntity livingEntity, @NotNull Grave grave, @NotNull Entity entity, @Nullable Block block
   ) {
      super(grave, entity, location, null, block, livingEntity, null);
   }

   public GraveProjectileHitEvent(@NotNull Location location, @NotNull Grave grave, @NotNull Entity entity, @Nullable Block block) {
      super(grave, entity, location, null, block, null, null);
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

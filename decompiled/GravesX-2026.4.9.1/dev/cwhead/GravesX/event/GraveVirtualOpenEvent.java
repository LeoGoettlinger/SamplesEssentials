package dev.cwhead.GravesX.event;

import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.event.graveevent.GraveEntityEvent;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GraveVirtualOpenEvent extends GraveEntityEvent {
   private static final HandlerList HANDLERS = new HandlerList();
   private final double distance;
   private double maxDistance;
   private boolean cancelled;

   public GraveVirtualOpenEvent(
      @NotNull Grave grave,
      @NotNull Entity entity,
      @Nullable Location location,
      @Nullable BlockData.BlockType blockType,
      @Nullable Block block,
      @Nullable LivingEntity livingEntity,
      @Nullable LivingEntity targetEntity,
      double distance,
      double maxDistance
   ) {
      super(grave, entity, location, blockType, block, livingEntity, targetEntity);
      this.distance = distance;
      this.maxDistance = maxDistance;
   }

   public double getDistance() {
      return this.distance;
   }

   public double getMaxDistance() {
      return this.maxDistance;
   }

   public void setMaxDistance(double maxDistance) {
      this.maxDistance = maxDistance;
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

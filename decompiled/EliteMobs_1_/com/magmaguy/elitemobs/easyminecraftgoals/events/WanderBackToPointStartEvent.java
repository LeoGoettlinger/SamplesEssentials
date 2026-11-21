package com.magmaguy.elitemobs.easyminecraftgoals.events;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import lombok.Generated;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class WanderBackToPointStartEvent extends Event implements Cancellable {
   private static final HandlerList handlers = new HandlerList();
   private final boolean hardObjective;
   private final LivingEntity livingEntity;
   private AbstractWanderBackToPoint abstractWanderBackToPoint;
   private boolean cancelled = false;

   public WanderBackToPointStartEvent(boolean hard, LivingEntity livingEntity, AbstractWanderBackToPoint abstractWanderBackToPoint) {
      this.hardObjective = hard;
      this.livingEntity = livingEntity;
      this.abstractWanderBackToPoint = abstractWanderBackToPoint;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public HandlerList getHandlers() {
      return handlers;
   }

   public boolean isCancelled() {
      return this.cancelled;
   }

   public void setCancelled(boolean cancel) {
      this.cancelled = cancel;
   }

   @Generated
   public boolean isHardObjective() {
      return this.hardObjective;
   }

   @Generated
   public LivingEntity getLivingEntity() {
      return this.livingEntity;
   }

   @Generated
   public AbstractWanderBackToPoint getAbstractWanderBackToPoint() {
      return this.abstractWanderBackToPoint;
   }
}

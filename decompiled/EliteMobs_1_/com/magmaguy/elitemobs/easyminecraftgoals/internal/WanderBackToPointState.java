package com.magmaguy.elitemobs.easyminecraftgoals.internal;

import com.magmaguy.elitemobs.easyminecraftgoals.events.WanderBackToPointEndEvent;
import com.magmaguy.elitemobs.easyminecraftgoals.events.WanderBackToPointStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public final class WanderBackToPointState {
   private final AbstractWanderBackToPoint wanderBackToPoint;
   private boolean active;
   private long startedAtMillis;

   public WanderBackToPointState(AbstractWanderBackToPoint wanderBackToPoint) {
      this.wanderBackToPoint = wanderBackToPoint;
   }

   public boolean begin() {
      if (this.active) {
         return false;
      } else {
         this.active = true;
         this.startedAtMillis = System.currentTimeMillis();
         WanderBackToPointStartEvent event = new WanderBackToPointStartEvent(
            this.wanderBackToPoint.isHardObjective(), this.wanderBackToPoint.getLivingEntity(), this.wanderBackToPoint
         );
         Bukkit.getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            return true;
         } else {
            this.active = false;
            this.startedAtMillis = 0L;
            return false;
         }
      }
   }

   public void end(boolean teleportOnFailure) {
      if (this.active) {
         this.active = false;
         this.startedAtMillis = 0L;
         LivingEntity livingEntity = this.wanderBackToPoint.getLivingEntity();
         if (teleportOnFailure && this.wanderBackToPoint.isTeleportOnFail() && livingEntity != null && livingEntity.isValid() && !livingEntity.isDead()) {
            livingEntity.teleport(this.wanderBackToPoint.getReturnLocation());
         }

         WanderBackToPointEndEvent event = new WanderBackToPointEndEvent(this.wanderBackToPoint.isHardObjective(), livingEntity, this.wanderBackToPoint);
         Bukkit.getPluginManager().callEvent(event);
      }
   }

   public boolean isActive() {
      return this.active;
   }

   public boolean hasTimedOut() {
      return this.active && System.currentTimeMillis() - this.startedAtMillis >= 50L * this.wanderBackToPoint.getMaxDurationTicks();
   }

   public boolean isAtReturnPoint() {
      LivingEntity livingEntity = this.wanderBackToPoint.getLivingEntity();
      Location returnLocation = this.wanderBackToPoint.getReturnLocation();
      if (livingEntity != null && returnLocation != null && returnLocation.getWorld() != null) {
         Location currentLocation = livingEntity.getLocation();
         if (currentLocation.getWorld() != null && currentLocation.getWorld().equals(returnLocation.getWorld())) {
            double acceptedDistance = Math.max(1.0, (double)this.wanderBackToPoint.getStopReturnDistance());
            return currentLocation.distanceSquared(returnLocation) <= acceptedDistance * acceptedDistance;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }
}

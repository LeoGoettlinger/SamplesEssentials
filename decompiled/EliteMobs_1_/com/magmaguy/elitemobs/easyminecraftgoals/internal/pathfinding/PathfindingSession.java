package com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding;

import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingStatus;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public final class PathfindingSession implements PathfindingHandle {
   private static final double ARRIVAL_DISTANCE_SQUARED = 0.25;
   private static final double PROGRESS_DISTANCE_SQUARED = 0.0025;
   private static final int STUCK_TICKS = 100;
   private final LivingEntity livingEntity;
   private final PathfindingDriver driver;
   private Location destination;
   private Location lastProgressLocation;
   private double speedModifier;
   private int consecutiveStuckTicks;
   private boolean restartRequested;
   private boolean pathPrepared;
   private boolean closed;
   private PathfindingStatus status = PathfindingStatus.IDLE;

   public PathfindingSession(LivingEntity livingEntity, PathfindingDriver driver) {
      this.livingEntity = Objects.requireNonNull(livingEntity, "livingEntity");
      this.driver = Objects.requireNonNull(driver, "driver");
   }

   @Override
   public boolean moveTo(Location target, double requestedSpeedModifier) {
      if (this.closed || !this.isValidTarget(target) || !isValidSpeed(requestedSpeedModifier)) {
         return false;
      } else if (this.livingEntity.isValid() && !this.livingEntity.isDead()) {
         this.destination = target.clone();
         this.speedModifier = requestedSpeedModifier;
         this.restartRequested = true;
         this.pathPrepared = false;
         this.resetProgress();
         this.driver.stopNavigation();
         this.status = PathfindingStatus.WAITING;
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void clearDestination() {
      if (!this.closed) {
         this.destination = null;
         this.restartRequested = false;
         this.pathPrepared = false;
         this.resetProgress();
         this.driver.stopNavigation();
         this.status = PathfindingStatus.IDLE;
      }
   }

   @Override
   public PathfindingStatus status() {
      return this.status;
   }

   @Override
   public void close() {
      if (!this.closed) {
         this.closed = true;
         this.destination = null;
         this.restartRequested = false;
         this.pathPrepared = false;
         this.resetProgress();
         this.status = PathfindingStatus.CLOSED;
         this.driver.stopNavigation();
         this.driver.unregisterGoal();
      }
   }

   public boolean canUse() {
      if (this.closed || this.destination == null) {
         return false;
      } else if (!this.entityStillExists()) {
         this.finish(PathfindingStatus.ENDED_SHORT);
         return false;
      } else if (!this.driver.canNavigate() || this.driver.hasCombatTarget()) {
         this.status = PathfindingStatus.WAITING;
         return false;
      } else if (this.hasArrived()) {
         this.finish(PathfindingStatus.ARRIVED);
         return false;
      } else {
         this.restartRequested = false;
         this.pathPrepared = this.driver.preparePath(this.destination);
         if (!this.pathPrepared) {
            this.finish(PathfindingStatus.NO_PATH);
            return false;
         } else {
            return true;
         }
      }
   }

   public void start() {
      if (!this.closed && this.destination != null && this.pathPrepared) {
         if (!this.driver.startPreparedPath(this.speedModifier)) {
            this.finish(PathfindingStatus.NO_PATH);
         } else {
            this.status = PathfindingStatus.MOVING;
            this.lastProgressLocation = this.livingEntity.getLocation();
            this.consecutiveStuckTicks = 0;
         }
      }
   }

   public void tick() {
      if (!this.closed && this.destination != null && this.status == PathfindingStatus.MOVING) {
         if (this.hasArrived()) {
            this.finish(PathfindingStatus.ARRIVED);
         } else {
            Location current = this.livingEntity.getLocation();
            if (this.lastProgressLocation != null
               && this.lastProgressLocation.getWorld() == current.getWorld()
               && !(current.distanceSquared(this.lastProgressLocation) >= 0.0025)) {
               if (this.consecutiveStuckTicks < Integer.MAX_VALUE) {
                  this.consecutiveStuckTicks++;
               }

               if (this.consecutiveStuckTicks >= 100) {
                  this.finish(PathfindingStatus.STUCK);
               }
            } else {
               this.lastProgressLocation = current;
               this.consecutiveStuckTicks = 0;
            }
         }
      }
   }

   public boolean canContinueToUse() {
      if (this.closed || this.destination == null) {
         return false;
      } else if (this.restartRequested) {
         this.status = PathfindingStatus.WAITING;
         return false;
      } else if (!this.entityStillExists()) {
         this.finish(PathfindingStatus.ENDED_SHORT);
         return false;
      } else if (!this.driver.canNavigate() || this.driver.hasCombatTarget()) {
         this.status = PathfindingStatus.WAITING;
         return false;
      } else if (this.hasArrived()) {
         this.finish(PathfindingStatus.ARRIVED);
         return false;
      } else if (this.status != PathfindingStatus.MOVING) {
         return false;
      } else if (this.driver.navigationDone()) {
         this.finish(PathfindingStatus.ENDED_SHORT);
         return false;
      } else {
         return true;
      }
   }

   public void stop() {
      this.driver.stopNavigation();
      this.pathPrepared = false;
      this.resetProgress();
      if (!this.closed && this.destination != null && this.status == PathfindingStatus.MOVING) {
         this.status = PathfindingStatus.WAITING;
      }
   }

   private boolean entityStillExists() {
      return this.livingEntity.isValid() && !this.livingEntity.isDead();
   }

   private boolean hasArrived() {
      return this.destination != null && this.livingEntity.getWorld() == this.destination.getWorld()
         ? this.livingEntity.getLocation().distanceSquared(this.destination) <= 0.25
         : false;
   }

   private void finish(PathfindingStatus terminalStatus) {
      this.destination = null;
      this.restartRequested = false;
      this.pathPrepared = false;
      this.resetProgress();
      this.status = terminalStatus;
      this.driver.stopNavigation();
   }

   private void resetProgress() {
      this.lastProgressLocation = null;
      this.consecutiveStuckTicks = 0;
   }

   private boolean isValidTarget(Location target) {
      return target != null
         && target.getWorld() != null
         && target.getWorld() == this.livingEntity.getWorld()
         && Double.isFinite(target.getX())
         && Double.isFinite(target.getY())
         && Double.isFinite(target.getZ());
   }

   private static boolean isValidSpeed(double requestedSpeedModifier) {
      return Double.isFinite(requestedSpeedModifier) && requestedSpeedModifier > 0.0;
   }
}

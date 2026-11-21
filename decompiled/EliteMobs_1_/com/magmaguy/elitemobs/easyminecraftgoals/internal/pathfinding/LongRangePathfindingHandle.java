package com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding;

import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingStatus;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public final class LongRangePathfindingHandle implements PathfindingHandle {
   private static final double ARRIVAL_DISTANCE_SQUARED = 0.25;
   private static final long TERRAIN_RETRY_MILLIS = 1000L;
   private static final int MAX_INTERNAL_REPLANS = 2;
   private final LivingEntity livingEntity;
   private final PathfindingHandle nativeHandle;
   private final Deque<Location> pendingWaypoints = new ArrayDeque<>();
   private LongRangePathPlanner.Request planningRequest;
   private Location finalTarget;
   private Location activeWaypoint;
   private List<Location> preview = List.of();
   private double speedModifier;
   private long terrainRetryAfterMillis;
   private int internalReplans;
   private boolean currentPlanReachesFinal;
   private boolean closed;
   private PathfindingStatus status = PathfindingStatus.IDLE;

   public LongRangePathfindingHandle(LivingEntity livingEntity, PathfindingHandle nativeHandle) {
      this.livingEntity = Objects.requireNonNull(livingEntity, "livingEntity");
      this.nativeHandle = Objects.requireNonNull(nativeHandle, "nativeHandle");
   }

   public static void shutdownPlanning() {
      requirePrimaryThread();
      LongRangePathPlanner.shutdown();
   }

   @Override
   public boolean moveTo(Location target, double requestedSpeedModifier) {
      requirePrimaryThread();
      if (!this.closed && this.validTarget(target) && validSpeed(requestedSpeedModifier) && this.bodyExists()) {
         this.resetRequest();
         this.finalTarget = target.clone();
         this.speedModifier = requestedSpeedModifier;
         this.internalReplans = 0;
         this.terrainRetryAfterMillis = 0L;
         Location start = this.livingEntity.getLocation();
         if (start.distance(target) <= 8.0) {
            this.preview = List.of(start.clone(), target.clone());
            return this.issueNative(target);
         } else {
            this.beginPlanning();
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void clearDestination() {
      requirePrimaryThread();
      if (!this.closed) {
         this.resetRequest();
         this.nativeHandle.clearDestination();
         this.status = PathfindingStatus.IDLE;
      }
   }

   @Override
   public PathfindingStatus status() {
      requirePrimaryThread();
      this.pump();
      return this.status;
   }

   @Override
   public List<Location> routePreview() {
      requirePrimaryThread();
      this.pump();
      return this.preview.stream().<Location>map(Location::clone).toList();
   }

   @Override
   public void close() {
      requirePrimaryThread();
      if (!this.closed) {
         this.closed = true;
         this.cancelPlanning();
         this.pendingWaypoints.clear();
         this.finalTarget = null;
         this.activeWaypoint = null;
         this.preview = List.of();
         this.nativeHandle.close();
         this.status = PathfindingStatus.CLOSED;
      }
   }

   private void pump() {
      if (!this.closed) {
         if (!this.bodyExists()) {
            this.finish(PathfindingStatus.ENDED_SHORT);
         } else if (this.planningRequest != null) {
            if (this.planningRequest.isCancelled()) {
               this.finish(PathfindingStatus.NO_PATH);
            }
         } else if (this.terrainRetryAfterMillis > 0L) {
            if (System.currentTimeMillis() >= this.terrainRetryAfterMillis && this.finalTarget != null) {
               this.beginPlanning();
            }
         } else if (this.activeWaypoint != null) {
            PathfindingStatus nativeStatus = this.nativeHandle.status();
            switch (nativeStatus) {
               case MOVING:
               case WAITING:
                  this.status = nativeStatus;
                  break;
               case ARRIVED:
                  this.activeWaypoint = null;
                  this.internalReplans = 0;
                  this.issueNextWaypoint();
                  break;
               case NO_PATH:
               case ENDED_SHORT:
               case STUCK:
                  this.nativeFailure(nativeStatus);
                  break;
               case CLOSED:
                  this.close();
                  break;
               case IDLE:
                  this.issueNative(this.activeWaypoint);
            }
         }
      }
   }

   private void nativeFailure(PathfindingStatus failure) {
      this.nativeHandle.clearDestination();
      this.activeWaypoint = null;
      if (this.finalTarget != null && this.internalReplans++ < 2) {
         this.beginPlanning();
      } else {
         this.finish(failure);
      }
   }

   private void beginPlanning() {
      if (!this.closed && this.finalTarget != null && this.bodyExists()) {
         this.cancelPlanning();
         this.terrainRetryAfterMillis = 0L;
         this.nativeHandle.clearDestination();
         this.pendingWaypoints.clear();
         this.activeWaypoint = null;
         this.preview = List.of();
         this.currentPlanReachesFinal = false;
         this.status = PathfindingStatus.WAITING;
         this.planningRequest = LongRangePathPlanner.plan(this.livingEntity, this.finalTarget, this::planReady);
      } else {
         this.finish(PathfindingStatus.ENDED_SHORT);
      }
   }

   private void planReady(LongRangePathPlanner.PlanResult result) {
      requirePrimaryThread();
      if (!this.closed && this.finalTarget != null) {
         this.planningRequest = null;
         switch (result.outcome()) {
            case FOUND:
               this.preview = result.preview();
               this.pendingWaypoints.clear();
               result.waypoints().stream().map(Location::clone).forEach(this.pendingWaypoints::addLast);
               this.currentPlanReachesFinal = result.reachesFinal();
               this.issueNextWaypoint();
               break;
            case TERRAIN_UNAVAILABLE:
               this.status = PathfindingStatus.WAITING;
               this.terrainRetryAfterMillis = System.currentTimeMillis() + 1000L;
               break;
            case NO_PATH:
               this.finish(PathfindingStatus.NO_PATH);
         }
      }
   }

   private void issueNextWaypoint() {
      while (!this.pendingWaypoints.isEmpty() && this.livingEntity.getLocation().distanceSquared(this.pendingWaypoints.getFirst()) <= 0.25) {
         this.pendingWaypoints.removeFirst();
      }

      if (!this.pendingWaypoints.isEmpty()) {
         this.issueNative(this.pendingWaypoints.removeFirst());
      } else if (this.currentPlanReachesFinal && this.finalTarget != null && this.livingEntity.getLocation().distanceSquared(this.finalTarget) <= 0.25) {
         this.finish(PathfindingStatus.ARRIVED);
      } else if (this.currentPlanReachesFinal && this.finalTarget != null) {
         this.issueNative(this.finalTarget);
      } else {
         this.beginPlanning();
      }
   }

   private boolean issueNative(Location waypoint) {
      if (waypoint != null && !this.closed) {
         this.activeWaypoint = waypoint.clone();
         boolean accepted = this.nativeHandle.moveTo(this.activeWaypoint, this.speedModifier);
         if (accepted) {
            this.status = PathfindingStatus.WAITING;
            return true;
         } else {
            this.activeWaypoint = null;
            this.finish(PathfindingStatus.NO_PATH);
            return false;
         }
      } else {
         return false;
      }
   }

   private void finish(PathfindingStatus terminalStatus) {
      this.cancelPlanning();
      this.pendingWaypoints.clear();
      this.finalTarget = null;
      this.activeWaypoint = null;
      this.currentPlanReachesFinal = false;
      this.terrainRetryAfterMillis = 0L;
      if (!this.closed && terminalStatus != PathfindingStatus.CLOSED) {
         this.nativeHandle.clearDestination();
      }

      this.status = terminalStatus;
   }

   private void resetRequest() {
      this.cancelPlanning();
      this.pendingWaypoints.clear();
      this.finalTarget = null;
      this.activeWaypoint = null;
      this.preview = List.of();
      this.currentPlanReachesFinal = false;
      this.terrainRetryAfterMillis = 0L;
   }

   private void cancelPlanning() {
      if (this.planningRequest != null) {
         this.planningRequest.close();
      }

      this.planningRequest = null;
   }

   private boolean validTarget(Location target) {
      return target != null
         && target.getWorld() != null
         && target.getWorld() == this.livingEntity.getWorld()
         && Double.isFinite(target.getX())
         && Double.isFinite(target.getY())
         && Double.isFinite(target.getZ());
   }

   private boolean bodyExists() {
      return this.livingEntity.isValid() && !this.livingEntity.isDead();
   }

   private static boolean validSpeed(double speed) {
      return Double.isFinite(speed) && speed > 0.0;
   }

   private static void requirePrimaryThread() {
      if (!Bukkit.isPrimaryThread()) {
         throw new IllegalStateException("PathfindingHandle methods must run on the server thread");
      }
   }
}

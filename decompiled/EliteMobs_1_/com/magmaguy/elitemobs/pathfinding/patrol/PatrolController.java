package com.magmaguy.elitemobs.pathfinding.patrol;

import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingStatus;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.pathfinding.Navigation;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;

final class PatrolController {
   private static final int MATERIALIZE_RETRY_TICKS = 20;
   private final PatrolActor actor;
   private final PatrolRoute route;
   private final PatrolStateStore stateStore;
   private PathfindingHandle driver;
   private UUID attachedBodyId;
   private PatrolRuntimeState state = PatrolRuntimeState.RESUMING;
   private int currentNode;
   private int targetNode;
   private int direction = 1;
   private int failures;
   private int materializeCooldown;
   private double fraction;
   private double virtualSpeed;
   private Location safeLocation;
   private boolean destinationAssigned;
   private boolean combatHeld;
   private boolean proximityHeld;
   private boolean lifecycleHeld;
   private boolean aiHeld;
   private boolean yieldedHeld;
   private boolean manuallyPaused;
   private Location manualTarget;
   private boolean holdAfterManualMove;
   private boolean terrainHeld;
   private boolean retired;
   private int startWaitTicks;
   private boolean startWaitServed;
   private List<Location> latestResolvedPath = List.of();
   private final Deque<Location> detachedPath = new ArrayDeque<>();
   private int persistentChunkX = Integer.MIN_VALUE;
   private int persistentChunkZ = Integer.MIN_VALUE;

   PatrolController(PatrolActor actor, PatrolStateStore stateStore) {
      this.actor = actor;
      this.route = actor.route();
      this.stateStore = stateStore;
      PatrolStateStore.StoredState restored = actor.persistsWhileDetached() ? stateStore.get(actor.canonicalIdentity(), this.route).orElse(null) : null;
      if (restored != null) {
         this.restore(restored);
      } else {
         this.initializeAtOrigin();
      }
   }

   PatrolActor actor() {
      return this.actor;
   }

   PatrolRuntimeState state() {
      return this.state;
   }

   boolean tick(long tick) {
      if (this.retired) {
         return true;
      } else {
         LivingEntity body = this.liveBody();
         this.proximityHeld = this.actor.isNearPlayer(tick);
         if (body == null) {
            if (this.driver != null || this.attachedBodyId != null) {
               this.detachBody();
            }

            if (!this.actor.persistsWhileDetached()) {
               this.actor.tickFacing(false);
               this.retired = true;
               this.state = PatrolRuntimeState.DISABLED;
               return true;
            }

            this.tickDetached();
         } else {
            if (!body.getUniqueId().equals(this.attachedBodyId)) {
               this.attachBody(body);
            }

            this.tickLoaded(body, tick);
         }

         this.actor.tickFacing(this.state == PatrolRuntimeState.PAUSED || this.state == PatrolRuntimeState.HELD);
         if (tick % 20L == 0L) {
            this.checkpoint();
         }

         return this.retired;
      }
   }

   void shutdown() {
      this.actor.tickFacing(false);
      this.closeDriver();
      this.checkpoint();
   }

   void retire(boolean discardPersistentState) {
      this.actor.tickFacing(false);
      this.retired = true;
      this.closeDriver();
      if (discardPersistentState) {
         this.stateStore.remove(this.actor.canonicalIdentity());
      } else {
         this.checkpoint();
      }
   }

   void setCombatHeld(boolean held) {
      this.combatHeld = held;
      if (held) {
         this.clearDestination();
         this.state = PatrolRuntimeState.YIELDED;
      } else if (!this.manuallyPaused && this.manualTarget == null) {
         this.resumeFromBody();
      }

      this.checkpoint();
   }

   void holdUntilBodyReplacement() {
      this.lifecycleHeld = true;
      this.clearDestination();
      this.checkpoint();
   }

   void worldUnloaded() {
      this.actor.tickFacing(false);
      this.checkpoint();
      this.closeDriver();
      this.attachedBodyId = null;
      this.latestResolvedPath = List.of();
      this.detachedPath.clear();
      this.terrainHeld = true;
      if (this.safeLocation != null) {
         this.safeLocation.setWorld(null);
      }

      this.state = PatrolRuntimeState.DETACHED;
   }

   void worldLoaded(World world) {
      if (world != null && this.actor.origin().worldName().equals(world.getName())) {
         if (this.safeLocation != null && this.safeLocation.getWorld() == null) {
            this.safeLocation.setWorld(world);
         }
      }
   }

   void pause() {
      this.manuallyPaused = true;
      this.clearDestination();
      this.state = PatrolRuntimeState.PAUSED;
      this.checkpoint();
   }

   void resume() {
      this.startWaitTicks = 0;
      this.manuallyPaused = false;
      this.manualTarget = null;
      this.holdAfterManualMove = false;
      this.resumeFromBody();
      this.checkpoint();
   }

   boolean moveToOffset(Vector offset, boolean holdOnArrival) {
      Location origin = this.actor.origin().resolve();
      LivingEntity body = this.liveBody();
      if (origin != null && body != null && offset != null) {
         Location target = origin.add(offset);
         if (body.getWorld() != target.getWorld()) {
            return false;
         } else {
            this.manuallyPaused = false;
            this.combatHeld = false;
            this.startWaitTicks = 0;
            this.manualTarget = target;
            this.holdAfterManualMove = holdOnArrival;
            this.failures = 0;
            this.destinationAssigned = false;
            this.state = PatrolRuntimeState.MANUAL_MOVING;
            return this.issueDestination(target);
         }
      } else {
         return false;
      }
   }

   boolean teleportToOffset(Vector offset) {
      Location origin = this.actor.origin().resolve();
      LivingEntity body = this.liveBody();
      if (origin != null && body != null && offset != null) {
         Location target = origin.add(offset);
         if (target.getWorld() == body.getWorld()
            && NMSManager.getAdapter() != null
            && NMSManager.getAdapter().isPositionEntityTicking(target)
            && body.teleport(target)) {
            this.startWaitTicks = 0;
            this.safeLocation = body.getLocation().clone();
            this.updatePersistentLocation(true);
            this.resumeFromBody();
            this.checkpoint();
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   boolean isActivelyMoving() {
      return this.state == PatrolRuntimeState.PATROLLING || this.state == PatrolRuntimeState.RESUMING || this.state == PatrolRuntimeState.MANUAL_MOVING;
   }

   Location logicalLocation() {
      LivingEntity body = this.liveBody();
      if (body != null) {
         return body.getLocation();
      } else if (this.manualTarget != null) {
         return this.safeLocation == null ? this.manualTarget.clone() : this.safeLocation.clone();
      } else {
         try {
            return this.route.interpolate(this.actor.origin(), this.currentNode, this.targetNode, this.fraction);
         } catch (RuntimeException var3) {
            return this.safeLocation == null ? null : this.safeLocation.clone();
         }
      }
   }

   Location materializationLocation() {
      return this.safeLocation == null ? null : this.safeLocation.clone();
   }

   Location leashAnchor(Location bodyLocation) {
      return this.route.closestPoint(this.actor.origin(), bodyLocation);
   }

   Map<String, Object> diagnostics() {
      Map<String, Object> diagnostics = new LinkedHashMap<>();
      diagnostics.put("state", this.state.name());
      diagnostics.put("currentNode", this.currentNode);
      diagnostics.put("targetNode", this.targetNode);
      diagnostics.put("direction", this.direction);
      diagnostics.put("fraction", this.fraction);
      diagnostics.put("clockFrozen", this.clockFrozen());
      diagnostics.put("holdReason", this.holdReason());
      diagnostics.put("bodyPresent", this.liveBody() != null);
      diagnostics.put("virtualSpeed", this.virtualSpeed);
      diagnostics.put("startWaitTicks", this.startWaitTicks);
      Location logical = this.logicalLocation();
      if (logical != null) {
         diagnostics.put("world", logical.getWorld() == null ? this.actor.origin().worldName() : logical.getWorld().getName());
         diagnostics.put("x", logical.getX());
         diagnostics.put("y", logical.getY());
         diagnostics.put("z", logical.getZ());
      }

      return Map.copyOf(diagnostics);
   }

   private void attachBody(LivingEntity body) {
      this.closeDriver();
      this.attachedBodyId = body.getUniqueId();
      this.lifecycleHeld = false;
      this.terrainHeld = false;
      this.detachedPath.clear();
      this.latestResolvedPath = List.of();
      this.safeLocation = body.getLocation().clone();
      this.updatePersistentLocation(true);
      if (!this.actor.prepareBody()) {
         this.state = PatrolRuntimeState.DISABLED;
         Logger.warn("Patrol disabled for " + this.actor.displayName() + ": " + this.actor.ineligibleReason());
      } else {
         this.driver = NMSManager.getAdapter() == null ? null : NMSManager.getAdapter().createPathfindingHandle(body).orElse(null);
         if (this.driver == null) {
            this.state = PatrolRuntimeState.DISABLED;
            Logger.warn("Patrol disabled for " + this.actor.displayName() + ": no native pathfinding adapter for " + body.getType());
         } else {
            double observedSpeed = this.actor.currentMovementSpeed() * this.route.speedModifier();
            if (this.route.virtualSpeed() != null) {
               this.virtualSpeed = this.route.virtualSpeed();
            } else if (Double.isFinite(observedSpeed) && observedSpeed > 0.0) {
               this.virtualSpeed = observedSpeed;
            }

            this.resumeFromBody();
         }
      }
   }

   private void detachBody() {
      LivingEntity body = this.liveBody();
      if (body != null) {
         this.safeLocation = body.getLocation().clone();
         if (this.targetNode != this.currentNode) {
            this.fraction = this.route.projectedFraction(this.actor.origin(), this.safeLocation, this.currentNode, this.targetNode);
         }
      }

      this.captureResolvedPath();
      this.prepareDetachedPath();
      this.closeDriver();
      this.attachedBodyId = null;
      this.state = PatrolRuntimeState.DETACHED;
      this.updatePersistentLocation(true);
      this.checkpoint();
   }

   private void tickLoaded(LivingEntity body, long tick) {
      this.actor.syncVisuals();
      this.safeLocation = body.getLocation().clone();
      this.terrainHeld = false;
      this.updatePersistentLocation(false);
      if (this.targetNode != this.currentNode && this.manualTarget == null) {
         this.fraction = this.route.projectedFraction(this.actor.origin(), this.safeLocation, this.currentNode, this.targetNode);
      }

      if (this.actor.owner() instanceof RegionalBossEntity regionalBoss) {
         Navigation.updateLeashAnchor(regionalBoss, this.leashAnchor(body.getLocation()));
      }

      if (this.driver != null && this.state != PatrolRuntimeState.DISABLED) {
         if (this.manuallyPaused) {
            this.clearDestination();
            this.state = PatrolRuntimeState.PAUSED;
         } else if (this.combatHeld || this.actor.isInCombat()) {
            this.clearDestination();
            this.state = PatrolRuntimeState.YIELDED;
         } else if (this.proximityHeld) {
            this.clearDestination();
            this.state = PatrolRuntimeState.HELD;
         } else {
            EliteEntity elite = EntityTracker.getEliteMobEntity(body);
            boolean aiEnabled = elite == null ? body.hasAI() : elite.isAIEnabled();
            boolean aware = elite == null ? !(body instanceof Mob mob && !mob.isAware()) : elite.isAware();
            if (aiEnabled && aware) {
               boolean resumingAfterAiHold = this.aiHeld;
               this.aiHeld = false;
               if (resumingAfterAiHold) {
                  this.resumeFromBody();
               } else if (this.startWaitTicks > 0 && this.manualTarget == null) {
                  this.startWaitTicks--;
                  this.clearDestination();
                  this.faceOriginHeading(body);
                  this.state = PatrolRuntimeState.HELD;
               } else {
                  PathfindingStatus status = this.driver.status();
                  if (tick % 5L == 0L) {
                     this.captureResolvedPath();
                  }

                  if (this.manualTarget != null) {
                     this.tickManual(status);
                  } else if (this.destinationAssigned && status != PathfindingStatus.IDLE) {
                     switch (status) {
                        case ARRIVED:
                           this.routeArrived();
                           break;
                        case NO_PATH:
                        case ENDED_SHORT:
                        case STUCK:
                           this.routeFailed();
                           break;
                        case CLOSED:
                           this.attachBody(body);
                           break;
                        case WAITING:
                           this.yieldedHeld = true;
                           this.state = PatrolRuntimeState.YIELDED;
                           break;
                        case MOVING:
                           this.yieldedHeld = false;
                           if (this.state != PatrolRuntimeState.RESUMING) {
                              this.state = PatrolRuntimeState.PATROLLING;
                           }
                           break;
                        case IDLE:
                           this.issueRouteDestination();
                     }
                  } else {
                     this.issueRouteDestination();
                  }
               }
            } else {
               this.aiHeld = true;
               this.clearDestination();
               this.state = PatrolRuntimeState.AI_OFF;
            }
         }
      }
   }

   private void tickManual(PathfindingStatus status) {
      if (this.destinationAssigned && status != PathfindingStatus.IDLE) {
         switch (status) {
            case ARRIVED:
               this.destinationAssigned = false;
               this.manualTarget = null;
               this.failures = 0;
               if (this.holdAfterManualMove) {
                  this.manuallyPaused = true;
                  this.state = PatrolRuntimeState.HELD;
               } else {
                  this.resumeFromBody();
               }

               this.checkpoint();
               break;
            case NO_PATH:
            case ENDED_SHORT:
            case STUCK:
               this.destinationAssigned = false;
               if (this.failures++ == 0) {
                  this.issueDestination(this.manualTarget);
               } else {
                  this.manualTarget = null;
                  this.manuallyPaused = this.holdAfterManualMove;
                  this.state = this.holdAfterManualMove ? PatrolRuntimeState.HELD : PatrolRuntimeState.PAUSED;
                  this.checkpoint();
               }
               break;
            case CLOSED:
               this.state = PatrolRuntimeState.DISABLED;
               break;
            case WAITING:
               this.yieldedHeld = true;
               this.state = PatrolRuntimeState.YIELDED;
               break;
            case MOVING:
               this.yieldedHeld = false;
               this.state = PatrolRuntimeState.MANUAL_MOVING;
               break;
            case IDLE:
               this.issueDestination(this.manualTarget);
         }
      } else {
         this.issueDestination(this.manualTarget);
      }
   }

   private void tickDetached() {
      this.state = PatrolRuntimeState.DETACHED;
      if (this.materializeCooldown > 0) {
         this.materializeCooldown--;
      }

      if (this.startWaitTicks > 0) {
         this.startWaitTicks--;
      } else if (!this.clockFrozen() && this.manualTarget == null && this.virtualSpeed > 0.0) {
         this.advanceDetachedPath(this.virtualSpeed);
      }

      this.updatePersistentLocation(false);
      if (this.safeLocation != null
         && this.materializeCooldown == 0
         && NMSManager.getAdapter() != null
         && NMSManager.getAdapter().isPositionEntityTicking(this.safeLocation)) {
         this.materializeCooldown = 20;
         this.actor.materialize();
      }
   }

   private void advanceDetachedPath(double blocks) {
      if (this.safeLocation != null && this.safeLocation.getWorld() != null && !this.detachedPath.isEmpty()) {
         this.terrainHeld = false;
         double remaining = blocks;
         int guard = 0;

         while (remaining > 0.0 && !this.detachedPath.isEmpty() && guard++ < 1024) {
            Location target = this.detachedPath.getFirst();
            if (target.getWorld() != this.safeLocation.getWorld()) {
               this.detachedPath.clear();
               this.terrainHeld = true;
               return;
            }

            Vector delta = target.toVector().subtract(this.safeLocation.toVector());
            double distance = delta.length();
            if (distance <= 1.0E-6) {
               this.detachedPath.removeFirst();
            } else if (remaining + 1.0E-9 < distance) {
               this.safeLocation.add(delta.multiply(remaining / distance));
               remaining = 0.0;
            } else {
               remaining -= distance;
               this.safeLocation = this.detachedPath.removeFirst().clone();
            }
         }

         this.fraction = this.route.projectedFraction(this.actor.origin(), this.safeLocation, this.currentNode, this.targetNode);
         this.updatePersistentLocation(false);
         Location authoredTarget = this.route.node(this.actor.origin(), this.targetNode);
         if (this.safeLocation.distanceSquared(authoredTarget) <= 0.25) {
            this.currentNode = this.targetNode;
            this.startWaitIfBackAtStart();
            this.fraction = 0.0;
            PatrolRoute.Step step = this.route.next(this.currentNode, this.direction);
            this.targetNode = step.targetNode();
            this.direction = step.direction();
            this.detachedPath.clear();
         }

         if (this.detachedPath.isEmpty()) {
            this.terrainHeld = true;
         }
      } else {
         this.terrainHeld = true;
      }
   }

   private void captureResolvedPath() {
      if (this.driver != null) {
         List<Location> resolved = this.driver.routePreview();
         this.latestResolvedPath = resolved.size() < 2 ? List.of() : resolved.stream().<Location>map(Location::clone).toList();
      }
   }

   private void prepareDetachedPath() {
      this.detachedPath.clear();
      if (this.safeLocation != null && this.safeLocation.getWorld() != null && !this.latestResolvedPath.isEmpty()) {
         int nextPoint = 1;
         double nearestDistance = Double.POSITIVE_INFINITY;

         for (int index = 0; index < this.latestResolvedPath.size() - 1; index++) {
            Location start = this.latestResolvedPath.get(index);
            Location end = this.latestResolvedPath.get(index + 1);
            if (start.getWorld() == this.safeLocation.getWorld() && end.getWorld() == this.safeLocation.getWorld()) {
               double distance = distanceSquaredToSegment(this.safeLocation, start, end);
               if (distance < nearestDistance) {
                  nextPoint = index + 1;
                  nearestDistance = distance;
               }
            }
         }

         for (int indexx = nextPoint; indexx < this.latestResolvedPath.size(); indexx++) {
            Location candidate = this.latestResolvedPath.get(indexx);
            if (candidate.getWorld() == this.safeLocation.getWorld()) {
               this.detachedPath.addLast(candidate.clone());
            }
         }

         this.terrainHeld = this.detachedPath.isEmpty();
      } else {
         this.terrainHeld = true;
      }
   }

   private static double distanceSquaredToSegment(Location point, Location start, Location end) {
      Vector segment = end.toVector().subtract(start.toVector());
      double lengthSquared = segment.lengthSquared();
      if (lengthSquared <= 1.0E-8) {
         return point.distanceSquared(start);
      } else {
         double fraction = point.toVector().subtract(start.toVector()).dot(segment) / lengthSquared;
         fraction = Math.max(0.0, Math.min(1.0, fraction));
         return point.toVector().distanceSquared(start.toVector().add(segment.multiply(fraction)));
      }
   }

   private void routeArrived() {
      this.destinationAssigned = false;
      this.failures = 0;
      this.currentNode = this.targetNode;
      this.startWaitIfBackAtStart();
      this.fraction = 0.0;
      this.safeLocation = this.route.node(this.actor.origin(), this.currentNode);
      this.latestResolvedPath = List.of();
      this.detachedPath.clear();
      PatrolRoute.Step step = this.route.next(this.currentNode, this.direction);
      this.targetNode = step.targetNode();
      this.direction = step.direction();
      this.state = PatrolRuntimeState.PATROLLING;
      this.updatePersistentLocation(true);
      this.checkpoint();
      if (this.startWaitTicks == 0) {
         this.issueRouteDestination();
      }
   }

   private void routeFailed() {
      this.destinationAssigned = false;
      if (this.bodyReachedTargetNode()) {
         this.routeArrived();
      } else if (this.failures++ == 0) {
         this.issueRouteDestination();
      } else {
         this.failures = 0;
         this.currentNode = this.targetNode;
         if (this.currentNode != 0) {
            this.startWaitServed = false;
         }

         PatrolRoute.Step step = this.route.next(this.currentNode, this.direction);
         this.targetNode = step.targetNode();
         this.direction = step.direction();
         this.fraction = 0.0;
         this.checkpoint();
         this.issueRouteDestination();
      }
   }

   private boolean bodyReachedTargetNode() {
      LivingEntity body = this.liveBody();
      if (body == null) {
         return false;
      } else {
         Location node = this.route.node(this.actor.origin(), this.targetNode);
         Location at = body.getLocation();
         if (at.getWorld() != node.getWorld()) {
            return false;
         } else {
            double dx = at.getX() - node.getX();
            double dz = at.getZ() - node.getZ();
            return dx * dx + dz * dz <= 1.0 && Math.abs(at.getY() - node.getY()) <= 1.0;
         }
      }
   }

   private boolean issueRouteDestination() {
      Location target;
      try {
         target = this.route.node(this.actor.origin(), this.targetNode);
      } catch (RuntimeException var3) {
         this.state = PatrolRuntimeState.DETACHED;
         return false;
      }

      return this.issueDestination(target);
   }

   private boolean issueDestination(Location target) {
      if (this.driver != null && target != null) {
         this.yieldedHeld = false;
         this.destinationAssigned = this.driver.moveTo(target, this.route.speedModifier());
         if (!this.destinationAssigned) {
            this.state = PatrolRuntimeState.DISABLED;
         }

         return this.destinationAssigned;
      } else {
         return false;
      }
   }

   private void resumeFromBody() {
      if (this.driver != null) {
         LivingEntity body = this.liveBody();
         if (body != null) {
            this.targetNode = this.route.nearestNode(this.actor.origin(), body.getLocation());
            this.fraction = 0.0;
            this.failures = 0;
            this.destinationAssigned = false;
            this.state = PatrolRuntimeState.RESUMING;
            this.issueRouteDestination();
         }
      }
   }

   private void clearDestination() {
      if (this.driver != null) {
         this.driver.clearDestination();
      }

      this.destinationAssigned = false;
   }

   private void closeDriver() {
      if (this.driver != null) {
         this.driver.close();
      }

      this.driver = null;
      this.destinationAssigned = false;
   }

   private LivingEntity liveBody() {
      LivingEntity body = this.actor.body();
      return body != null && body.isValid() && !body.isDead() ? body : null;
   }

   private void initializeAtOrigin() {
      this.currentNode = 0;
      this.targetNode = this.route.next(0, 1).targetNode();
      this.direction = 1;
      this.fraction = 0.0;
      this.virtualSpeed = this.route.virtualSpeed() == null ? 0.0 : this.route.virtualSpeed();
      this.safeLocation = this.route.node(this.actor.origin(), this.currentNode);
   }

   private void restore(PatrolStateStore.StoredState restored) {
      this.currentNode = this.validNode(restored.currentNode()) ? restored.currentNode() : 0;
      this.targetNode = this.validNode(restored.targetNode()) ? restored.targetNode() : this.route.next(this.currentNode, 1).targetNode();
      if (this.targetNode == this.currentNode) {
         this.targetNode = this.route.next(this.currentNode, restored.direction()).targetNode();
      }

      this.direction = restored.direction() < 0 ? -1 : 1;
      this.fraction = Math.max(0.0, Math.min(1.0, restored.fraction()));
      this.startWaitServed = this.currentNode == 0;
      this.virtualSpeed = this.route.virtualSpeed() == null ? restored.virtualSpeed() : this.route.virtualSpeed();
      this.safeLocation = restored.safeLocation() == null ? null : restored.safeLocation().resolve();
      if (this.safeLocation == null || this.safeLocation.getWorld() == null || !this.actor.origin().worldName().equals(this.safeLocation.getWorld().getName())) {
         this.safeLocation = this.route.node(this.actor.origin(), this.currentNode);
      }
   }

   private void startWaitIfBackAtStart() {
      if (this.currentNode != 0) {
         this.startWaitServed = false;
      } else if (!this.startWaitServed) {
         this.startWaitServed = true;
         this.startWaitTicks = this.route.startNodeWaitTicks();
      }
   }

   private void faceOriginHeading(LivingEntity body) {
      float yaw = this.actor.origin().yaw();
      float pitch = this.actor.origin().pitch();
      Location location = body.getLocation();
      float yawDelta = Math.abs(((location.getYaw() - yaw) % 360.0F + 540.0F) % 360.0F - 180.0F);
      if (yawDelta > 1.0F || Math.abs(location.getPitch() - pitch) > 1.0F) {
         body.setRotation(yaw, pitch);
      }
   }

   private boolean validNode(int node) {
      return node >= 0 && node < this.route.size();
   }

   private void updatePersistentLocation(boolean force) {
      if (this.safeLocation != null && this.safeLocation.getWorld() != null) {
         int chunkX = this.safeLocation.getBlockX() >> 4;
         int chunkZ = this.safeLocation.getBlockZ() >> 4;
         if (force || chunkX != this.persistentChunkX || chunkZ != this.persistentChunkZ) {
            this.persistentChunkX = chunkX;
            this.persistentChunkZ = chunkZ;
            this.actor.updatePersistentLocation(this.safeLocation.clone());
         }
      }
   }

   private void checkpoint() {
      if (this.actor.persistsWhileDetached()) {
         this.stateStore
            .put(
               this.actor.canonicalIdentity(),
               new PatrolStateStore.StoredState(
                  this.actor.canonicalIdentity(),
                  this.currentNode,
                  this.targetNode,
                  this.direction,
                  this.fraction,
                  this.virtualSpeed,
                  PatrolStateStore.StoredLocation.from(this.safeLocation, this.actor.origin().worldName()),
                  this.route.geometryKey()
               )
            );
      }
   }

   private boolean clockFrozen() {
      return this.combatHeld
         || this.proximityHeld
         || this.lifecycleHeld
         || this.aiHeld
         || this.yieldedHeld
         || this.manuallyPaused
         || this.terrainHeld
         || this.state == PatrolRuntimeState.HELD
         || this.state == PatrolRuntimeState.AI_OFF;
   }

   private String holdReason() {
      if (this.combatHeld) {
         return "combat";
      } else if (this.proximityHeld) {
         return "nearby_player";
      } else if (this.lifecycleHeld) {
         return "body_replacement";
      } else if (this.aiHeld) {
         return "ai_off";
      } else if (this.yieldedHeld) {
         return "yielded";
      } else if (this.terrainHeld) {
         return "terrain_unavailable";
      } else if (this.startWaitTicks > 0) {
         return "start_node_wait";
      } else if (this.state == PatrolRuntimeState.AI_OFF) {
         return "ai_off";
      } else if (this.state == PatrolRuntimeState.HELD) {
         return "script_hold";
      } else {
         return this.manuallyPaused ? "manual" : "";
      }
   }
}

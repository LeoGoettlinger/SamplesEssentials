package com.magmaguy.elitemobs.easyminecraftgoals.v26.mind;

import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingStatus;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.PathfindingDriver;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.PathfindingSession;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;

final class NativeMindPathfindingHandle implements PathfindingHandle, PathfindingDriver {
   private final NativeMindSession owner;
   private final Mob mob;
   private final PathfindingSession pathfinding;
   private final NativeMindMovementOverrideGate gate;
   private NativeMindMovementOverrideGate.Lease lease;
   private Path path;
   private boolean blocked;
   private boolean running;

   NativeMindPathfindingHandle(NativeMindSession owner, NativeMindBody body, NativeMindMovementOverrideGate gate) {
      this.owner = owner;
      this.mob = body.mob();
      this.gate = gate;
      this.pathfinding = new PathfindingSession(body.entity(), this);
   }

   void tick(boolean blocked) {
      this.blocked = blocked;
      if (this.running && !this.pathfinding.canContinueToUse()) {
         this.pathfinding.stop();
      }

      if (!this.running && this.pathfinding.canUse()) {
         this.pathfinding.start();
      }

      if (this.running) {
         this.pathfinding.tick();
      }
   }

   @Override
   public boolean moveTo(Location destination, double speedModifier) {
      return this.pathfinding.moveTo(destination, speedModifier);
   }

   @Override
   public void clearDestination() {
      this.pathfinding.clearDestination();
   }

   @Override
   public PathfindingStatus status() {
      return this.pathfinding.status();
   }

   @Override
   public void close() {
      this.pathfinding.close();
   }

   @Override
   public boolean canNavigate() {
      return !this.blocked && this.mob.isAlive() && !this.mob.isRemoved();
   }

   @Override
   public boolean hasCombatTarget() {
      return this.mob.getTarget() != null;
   }

   @Override
   public boolean preparePath(Location destination) {
      this.path = this.mob.getNavigation().createPath(destination.getX(), destination.getY(), destination.getZ(), 0);
      return this.path != null && this.path.canReach();
   }

   @Override
   public boolean startPreparedPath(double speedModifier) {
      if (this.path == null) {
         return false;
      } else {
         this.lease = this.gate.acquire();
         if (!this.mob.getNavigation().moveTo(this.path, speedModifier)) {
            this.lease.close();
            this.lease = null;
            return false;
         } else {
            this.running = true;
            return true;
         }
      }
   }

   @Override
   public boolean navigationDone() {
      return this.mob.getNavigation().isDone();
   }

   @Override
   public void stopNavigation() {
      if (this.lease != null) {
         this.mob.getNavigation().stop();
         NativeMindVersion.stopControl(this.mob.getMoveControl());
         this.lease.close();
         this.lease = null;
      }

      this.running = false;
      this.path = null;
   }

   @Override
   public void unregisterGoal() {
      this.owner.releasePathfinding(this);
   }
}

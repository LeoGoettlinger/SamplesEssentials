package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R4.mind;

import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingStatus;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.PathfindingDriver;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.PathfindingSession;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.level.pathfinder.PathEntity;
import org.bukkit.Location;

final class NativeMindPathfindingHandle implements PathfindingHandle, PathfindingDriver {
   private final NativeMindSession owner;
   private final EntityInsentient mob;
   private final PathfindingSession pathfinding;
   private final NativeMindMovementOverrideGate gate;
   private NativeMindMovementOverrideGate.Lease lease;
   private PathEntity path;
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
      return !this.blocked && this.mob.bJ() && !this.mob.dQ();
   }

   @Override
   public boolean hasCombatTarget() {
      return this.mob.e() != null;
   }

   @Override
   public boolean preparePath(Location destination) {
      this.path = this.mob.N().a(destination.getX(), destination.getY(), destination.getZ(), 0);
      return this.path != null && this.path.j();
   }

   @Override
   public boolean startPreparedPath(double speedModifier) {
      if (this.path == null) {
         return false;
      } else {
         this.lease = this.gate.acquire();
         if (!this.mob.N().a(this.path, speedModifier)) {
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
      return this.mob.N().k();
   }

   @Override
   public void stopNavigation() {
      if (this.lease != null) {
         this.mob.N().m();
         NativeMindVersion.stopControl(this.mob.K());
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

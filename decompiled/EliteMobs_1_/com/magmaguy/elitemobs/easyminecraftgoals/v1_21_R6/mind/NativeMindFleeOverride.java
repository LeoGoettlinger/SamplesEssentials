package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R6.mind;

import com.magmaguy.elitemobs.easyminecraftgoals.TransientMovementOverride;
import com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R6.flee.FleePathfinder;
import java.util.Objects;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.level.pathfinder.PathEntity;
import org.bukkit.Location;

final class NativeMindFleeOverride implements TransientMovementOverride {
   private static final int REPATH_INTERVAL_TICKS = 5;
   private final NativeMindSession session;
   private final EntityCreature mob;
   private final double speedModifier;
   private final NativeMindMovementOverrideGate.Lease gateLease;
   private Location threatLocation;
   private PathEntity path;
   private int repathTicks;
   private boolean active = true;

   NativeMindFleeOverride(
      NativeMindSession session,
      EntityCreature mob,
      Location threatLocation,
      double speedModifier,
      PathEntity initialPath,
      NativeMindMovementOverrideGate.Lease gateLease
   ) {
      this.session = Objects.requireNonNull(session, "session");
      this.mob = Objects.requireNonNull(mob, "mob");
      if (Double.isFinite(speedModifier) && !(speedModifier <= 0.0)) {
         this.speedModifier = speedModifier;
         this.path = Objects.requireNonNull(initialPath, "initialPath");
         this.repathTicks = 5;
         this.gateLease = Objects.requireNonNull(gateLease, "gateLease");
         if (!this.retarget(threatLocation)) {
            gateLease.close();
            throw new IllegalArgumentException("Threat must be in the fleeing entity's world");
         }
      } else {
         throw new IllegalArgumentException("speedModifier must be finite and positive");
      }
   }

   void tick() {
      if (this.isActive() && this.session.hasBody(this.mob.cT())) {
         if (--this.repathTicks <= 0) {
            this.repathTicks = 5;
            this.path = FleePathfinder.findPath(this.mob, this.threatLocation);
         }

         if (this.path != null) {
            this.mob.S().a(this.path, this.speedModifier);
         }
      } else {
         this.close();
      }
   }

   @Override
   public boolean isActive() {
      return this.active && this.mob.bX() && !this.mob.ec();
   }

   @Override
   public boolean retarget(Location nextThreat) {
      NativeMindHost.requirePrimaryThread();
      if (this.active && nextThreat != null && nextThreat.getWorld() != null) {
         if (nextThreat.getWorld() != this.mob.getBukkitEntity().getWorld()) {
            return false;
         } else {
            if (this.threatLocation != null && this.threatLocation.distanceSquared(nextThreat) > 0.0) {
               this.repathTicks = 0;
            }

            this.threatLocation = nextThreat.clone();
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void close() {
      NativeMindHost.requirePrimaryThread();
      if (this.active) {
         this.active = false;
         this.session.removeMovementOverride(this);
         this.gateLease.close();
         this.path = null;
      }
   }
}

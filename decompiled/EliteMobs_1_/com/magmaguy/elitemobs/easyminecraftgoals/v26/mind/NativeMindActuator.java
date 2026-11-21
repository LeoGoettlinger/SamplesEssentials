package com.magmaguy.elitemobs.easyminecraftgoals.v26.mind;

import com.magmaguy.elitemobs.magmacore.ai.MindActuator;
import com.magmaguy.elitemobs.magmacore.ai.MindBodyLocomotion;
import com.magmaguy.elitemobs.magmacore.ai.MindControl;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.util.Vector;

final class NativeMindActuator {
   private final NativeMindBody body;
   private final Mob mob;
   private final NativeMindNavigationTracker navigation;
   private final BooleanSupplier movementOverridden;

   NativeMindActuator(NativeMindBody body, NativeMindNavigationTracker navigation, BooleanSupplier movementOverridden) {
      this.body = body;
      this.mob = body.mob();
      this.navigation = navigation;
      this.movementOverridden = movementOverridden;
   }

   MindActuator guarded(NativeMindBehaviorControl owner, NativeMindControlLeaseBook leases) {
      return new NativeMindActuator.Guarded(owner, leases);
   }

   void stopAll() {
      this.body.stopMovement();
      this.setNativeTarget(null);
      this.mob.setAggressive(false);
      this.navigation.reset();
   }

   private void setNativeTarget(LivingEntity target) {
      if (this.mob.getTarget() != target) {
         EntityTargetLivingEntityEvent event = new EntityTargetLivingEntityEvent(
            this.body.entity(), target == null ? null : (org.bukkit.entity.LivingEntity)target.getBukkitEntity(), TargetReason.CUSTOM
         );
         Bukkit.getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            ((org.bukkit.entity.Mob)this.body.entity()).setTarget(event.getTarget());
         }
      }
   }

   private void requireSameWorld(Location location) {
      if (location.getWorld() == null || location.getWorld() != this.mob.getBukkitEntity().getWorld()) {
         throw new IllegalArgumentException("Mind actuator destination must be in the body's world");
      }
   }

   private final class Guarded implements MindActuator {
      private final NativeMindBehaviorControl owner;
      private final NativeMindControlLeaseBook leases;

      private Guarded(NativeMindBehaviorControl owner, NativeMindControlLeaseBook leases) {
         this.owner = owner;
         this.leases = leases;
      }

      @Override
      public boolean moveTo(Location destination, double speedModifier) {
         this.require(MindControl.MOVE);
         if (NativeMindActuator.this.movementOverridden.getAsBoolean()) {
            return false;
         } else {
            Objects.requireNonNull(destination, "destination");
            if (Double.isFinite(speedModifier) && !(speedModifier <= 0.0)) {
               NativeMindActuator.this.requireSameWorld(destination);
               boolean started = NativeMindActuator.this.body.profile().locomotion() != MindBodyLocomotion.STATIONARY
                  && NativeMindActuator.this.mob.getNavigation().moveTo(destination.getX(), destination.getY(), destination.getZ(), speedModifier);
               NativeMindActuator.this.navigation.destinationRequested(destination);
               return started;
            } else {
               throw new IllegalArgumentException("speedModifier must be finite and positive");
            }
         }
      }

      @Override
      public void stopMoving() {
         this.require(MindControl.MOVE);
         if (!NativeMindActuator.this.movementOverridden.getAsBoolean()) {
            NativeMindActuator.this.body.stopMovement();
            NativeMindActuator.this.navigation.reset();
         }
      }

      @Override
      public boolean steerFlight(Vector velocity) {
         this.require(MindControl.MOVE);
         this.require(MindControl.LOOK);
         Objects.requireNonNull(velocity, "velocity");
         if (Double.isFinite(velocity.getX()) && Double.isFinite(velocity.getY()) && Double.isFinite(velocity.getZ())) {
            if (!NativeMindActuator.this.movementOverridden.getAsBoolean() && NativeMindActuator.this.body.profile().locomotion() == MindBodyLocomotion.FLYING) {
               NativeMindActuator.this.body.steerFlight(new Vec3(velocity.getX(), velocity.getY(), velocity.getZ()));
               return true;
            } else {
               return false;
            }
         } else {
            throw new IllegalArgumentException("Flight velocity must be finite");
         }
      }

      @Override
      public void lookAt(Location destination) {
         this.require(MindControl.LOOK);
         Objects.requireNonNull(destination, "destination");
         NativeMindActuator.this.requireSameWorld(destination);
         NativeMindActuator.this.mob.getLookControl().setLookAt(destination.getX(), destination.getY(), destination.getZ());
      }

      @Override
      public void jump() {
         this.require(MindControl.JUMP);
         if (!NativeMindActuator.this.movementOverridden.getAsBoolean()) {
            if (NativeMindActuator.this.body.profile().locomotion() != MindBodyLocomotion.STATIONARY) {
               NativeMindActuator.this.mob.getJumpControl().jump();
            }
         }
      }

      @Override
      public void setTarget(org.bukkit.entity.LivingEntity target) {
         this.require(MindControl.TARGET);
         Objects.requireNonNull(target, "target");
         LivingEntity nativeTarget = NativeMindVersion.getNMSLivingEntity(target);
         if (nativeTarget.level() != NativeMindActuator.this.mob.level()) {
            throw new IllegalArgumentException("Mind target must be in the body's world");
         } else {
            NativeMindActuator.this.setNativeTarget(nativeTarget);
         }
      }

      @Override
      public void clearTarget() {
         this.require(MindControl.TARGET);
         NativeMindActuator.this.setNativeTarget(null);
      }

      @Override
      public void attack(org.bukkit.entity.LivingEntity target) {
         this.require(MindControl.ATTACK);
         Objects.requireNonNull(target, "target");
         LivingEntity nativeTarget = NativeMindVersion.getNMSLivingEntity(target);
         if (nativeTarget.level() != NativeMindActuator.this.mob.level()) {
            throw new IllegalArgumentException("Mind target must be in the body's world");
         } else {
            org.bukkit.entity.LivingEntity bodyEntity = (org.bukkit.entity.LivingEntity)NativeMindActuator.this.mob.getBukkitEntity();
            if (NativeMindActuator.this.mob.getUsedItemHand() == InteractionHand.OFF_HAND) {
               bodyEntity.swingOffHand();
            } else {
               bodyEntity.swingMainHand();
            }

            NativeMindActuator.this.mob.doHurtTarget((ServerLevel)NativeMindActuator.this.mob.level(), nativeTarget);
         }
      }

      @Override
      public void stopAll() {
         if (this.owner == null) {
            throw new IllegalStateException("Mind sensors cannot use actuators");
         } else {
            if (this.holds(MindControl.MOVE) && !NativeMindActuator.this.movementOverridden.getAsBoolean()) {
               NativeMindActuator.this.body.stopMovement();
               NativeMindActuator.this.navigation.reset();
            }

            if (this.holds(MindControl.TARGET)) {
               NativeMindActuator.this.setNativeTarget(null);
            }

            if (this.holds(MindControl.ATTACK)) {
               NativeMindActuator.this.mob.setAggressive(false);
            }
         }
      }

      private void require(MindControl control) {
         NativeMindHost.requirePrimaryThread();
         if (!this.holds(control)) {
            throw new IllegalStateException("Mind behavior must declare and hold the " + control + " control");
         }
      }

      private boolean holds(MindControl control) {
         return this.owner != null && this.leases.holds(this.owner, control);
      }
   }
}

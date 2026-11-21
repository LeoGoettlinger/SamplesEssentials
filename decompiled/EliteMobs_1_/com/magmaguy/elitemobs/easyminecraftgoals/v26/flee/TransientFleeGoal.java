package com.magmaguy.elitemobs.easyminecraftgoals.v26.flee;

import com.magmaguy.elitemobs.easyminecraftgoals.TransientMovementOverride;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.internal.GoalSelectorAccess;
import java.util.EnumSet;
import java.util.Objects;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.World;

public final class TransientFleeGoal extends Goal implements TransientMovementOverride {
   public static final int PRIORITY = -2;
   private static final int REPATH_INTERVAL_TICKS = 5;
   private final PathfinderMob mob;
   private final double speedModifier;
   private final World world;
   private final TransientGoalRegistration registration;
   private Location threatLocation;
   private Path path;
   private int repathTicks;
   private boolean active = true;

   public TransientFleeGoal(PathfinderMob mob, World world, Location threatLocation, double speedModifier) {
      this.mob = Objects.requireNonNull(mob, "mob");
      this.world = Objects.requireNonNull(world, "world");
      this.registration = new TransientGoalRegistration(() -> GoalSelectorAccess.get(mob).addGoal(-2, this), () -> GoalSelectorAccess.get(mob).removeGoal(this));
      validateSpeed(speedModifier);
      this.speedModifier = speedModifier;
      if (!this.retarget(threatLocation)) {
         throw new IllegalArgumentException("Threat must be in the fleeing entity's world");
      } else {
         this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
      }
   }

   public void register() {
      if (this.active) {
         this.registration.register();
      }
   }

   public boolean canUse() {
      if (!this.canContinue()) {
         return false;
      } else {
         this.path = FleePathfinder.findPath(this.mob, this.threatLocation);
         return this.path != null;
      }
   }

   public boolean canContinueToUse() {
      return this.canContinue();
   }

   public boolean isInterruptable() {
      return false;
   }

   public void start() {
      this.repathTicks = 5;
      this.moveAlong(this.path);
   }

   public void tick() {
      if (--this.repathTicks <= 0 || this.mob.getNavigation().isDone()) {
         this.repathTicks = 5;
         this.path = FleePathfinder.findPath(this.mob, this.threatLocation);
         this.moveAlong(this.path);
      }
   }

   public void stop() {
      this.mob.getNavigation().stop();
      this.path = null;
   }

   @Override
   public boolean isActive() {
      return this.active;
   }

   @Override
   public boolean retarget(Location nextThreat) {
      if (this.active && nextThreat != null && nextThreat.getWorld() != null) {
         if (nextThreat.getWorld() != this.world) {
            return false;
         } else {
            this.threatLocation = nextThreat.clone();
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void close() {
      if (this.active) {
         this.active = false;
         this.registration.close();
      }
   }

   private boolean canContinue() {
      return this.active && this.mob.isAlive() && !this.mob.isRemoved() && !this.mob.isNoAi();
   }

   private void moveAlong(Path nextPath) {
      if (nextPath != null) {
         this.mob.getNavigation().moveTo(nextPath, this.speedModifier);
      }
   }

   private static void validateSpeed(double speedModifier) {
      if (!Double.isFinite(speedModifier) || speedModifier <= 0.0) {
         throw new IllegalArgumentException("speedModifier must be finite and positive");
      }
   }
}

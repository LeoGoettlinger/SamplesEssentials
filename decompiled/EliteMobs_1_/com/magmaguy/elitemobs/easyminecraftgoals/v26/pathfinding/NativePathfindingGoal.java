package com.magmaguy.elitemobs.easyminecraftgoals.v26.pathfinding;

import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingStatus;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.PathfindingDriver;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.PathfindingSession;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.internal.GoalSelectorAccess;
import java.util.EnumSet;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public final class NativePathfindingGoal extends Goal implements PathfindingHandle, PathfindingDriver {
   private final PathfinderMob mob;
   private final int priority;
   private final PathfindingSession session;
   private Path path;

   public NativePathfindingGoal(PathfinderMob mob, LivingEntity livingEntity, int priority) {
      this.mob = mob;
      this.priority = priority;
      this.session = new PathfindingSession(livingEntity, this);
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
   }

   public void register() {
      GoalSelectorAccess.get(this.mob).addGoal(this.priority, this);
   }

   public boolean canUse() {
      return this.session.canUse();
   }

   public boolean canContinueToUse() {
      return this.session.canContinueToUse();
   }

   public void start() {
      this.session.start();
   }

   public void tick() {
      this.session.tick();
   }

   public void stop() {
      this.session.stop();
   }

   public boolean isInterruptable() {
      return true;
   }

   @Override
   public boolean moveTo(Location target, double speedModifier) {
      return this.session.moveTo(target, speedModifier);
   }

   @Override
   public void clearDestination() {
      this.session.clearDestination();
   }

   @Override
   public PathfindingStatus status() {
      return this.session.status();
   }

   @Override
   public void close() {
      this.session.close();
   }

   @Override
   public boolean canNavigate() {
      return this.mob.isAlive() && !this.mob.isRemoved() && !this.mob.isNoAi();
   }

   @Override
   public boolean hasCombatTarget() {
      return this.mob.getTarget() != null;
   }

   @Override
   public boolean preparePath(Location destination) {
      double dx = destination.getX() - this.mob.getX();
      double dy = destination.getY() - this.mob.getY();
      double dz = destination.getZ() - this.mob.getZ();
      PathNavigation navigation = this.mob.getNavigation();
      navigation.setRequiredPathLength((float)Math.sqrt(dx * dx + dy * dy + dz * dz));

      try {
         this.path = navigation.createPath(destination.getX(), destination.getY(), destination.getZ(), 0);
      } finally {
         navigation.setRequiredPathLength(0.0F);
      }

      return this.path != null && this.path.canReach();
   }

   @Override
   public boolean startPreparedPath(double speedModifier) {
      return this.path != null && this.mob.getNavigation().moveTo(this.path, speedModifier);
   }

   @Override
   public boolean navigationDone() {
      return this.mob.getNavigation().isDone();
   }

   @Override
   public void stopNavigation() {
      this.mob.getNavigation().stop();
      this.path = null;
   }

   @Override
   public void unregisterGoal() {
      GoalSelectorAccess.get(this.mob).removeGoal(this);
   }
}

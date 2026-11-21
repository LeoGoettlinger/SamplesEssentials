package com.magmaguy.elitemobs.easyminecraftgoals.v26.wanderbacktopoint;

import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.WanderBackToPointState;
import com.magmaguy.elitemobs.easyminecraftgoals.utils.Utils;
import com.magmaguy.elitemobs.easyminecraftgoals.v26.internal.GoalSelectorAccess;
import java.util.EnumSet;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.Goal.Flag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

public class WanderBackToPointGoal extends Goal implements AbstractWanderBackToPoint {
   private final double maximumDistanceFromPoint;
   private final LivingEntity livingEntity;
   private final PathfinderMob pathfinderMob;
   private final Mob mob;
   Location returnLocation;
   private int priority;
   private Path path = null;
   private long lastTime;
   private final WanderBackToPointState returnState = new WanderBackToPointState(this);
   private float speed;
   private int stopReturnDistance = 0;
   private int goalRefreshCooldownTicks = 60;
   private int maxDurationTicks = 100;
   private boolean hardObjective = false;
   private boolean returnDuringCombat = false;
   private boolean teleportOnFail = false;
   private boolean startWithCooldown = false;

   public WanderBackToPointGoal(
      Mob mob, LivingEntity livingEntity, PathfinderMob pathfinderMob, Location location, double maximumDistanceFromPoint, int priority, int maxDurationTicks
   ) {
      this.mob = mob;
      this.livingEntity = livingEntity;
      this.pathfinderMob = pathfinderMob;
      this.returnLocation = location;
      this.priority = priority;
      this.maxDurationTicks = maxDurationTicks;
      this.speed = (float)livingEntity.getAttribute(Attribute.MOVEMENT_SPEED).getValue();
      this.maximumDistanceFromPoint = maximumDistanceFromPoint;
      this.lastTime = 0L;
      this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
   }

   public void start() {
      if (this.returnState.isActive() && this.pathfinderMob != null && this.path != null) {
         if (!this.pathfinderMob.getNavigation().moveTo(this.path, this.speed)) {
            this.returnState.end(true);
         }
      }
   }

   public void tick() {
   }

   public void stop() {
      boolean teleportOnFailure = this.returnState.isActive() && !this.returnState.isAtReturnPoint();
      if (this.pathfinderMob != null) {
         this.pathfinderMob.getNavigation().stop();
         this.path = null;
      }

      this.returnState.end(teleportOnFailure);
      this.updateCooldown();
   }

   public boolean canUse() {
      if (!this.hardObjective && !this.returnDuringCombat && this.mob.getTarget() instanceof Player) {
         this.updateCooldown();
         return false;
      } else if (this.lastTime + 50L * this.goalRefreshCooldownTicks - System.currentTimeMillis() > 0L) {
         return false;
      } else {
         this.updateCooldown();
         if (Utils.distanceShorterThan(this.returnLocation.toVector(), this.livingEntity.getLocation().toVector(), this.maximumDistanceFromPoint)) {
            return false;
         } else {
            this.path = this.pathfinderMob == null
               ? null
               : this.pathfinderMob
                  .getNavigation()
                  .createPath(this.returnLocation.getX(), this.returnLocation.getY(), this.returnLocation.getZ(), this.stopReturnDistance);
            if (!this.returnState.begin()) {
               this.path = null;
               return false;
            } else if (this.path != null && this.path.canReach()) {
               return true;
            } else {
               this.path = null;
               this.returnState.end(true);
               return false;
            }
         }
      }
   }

   public boolean canContinueToUse() {
      if (!this.returnState.isActive() || !this.livingEntity.isValid() || this.livingEntity.isDead()) {
         return false;
      } else if (this.returnState.hasTimedOut()) {
         return false;
      } else {
         return !this.hardObjective && !this.returnDuringCombat && this.mob.getTarget() instanceof Player
            ? false
            : this.pathfinderMob != null && this.path != null && this.path.canReach() && !this.pathfinderMob.getNavigation().isDone();
      }
   }

   public boolean isInterruptable() {
      return !this.hardObjective && !this.returnDuringCombat;
   }

   @Override
   public double getMaximumDistanceFromPoint() {
      return this.maximumDistanceFromPoint;
   }

   @Override
   public long getLastTime() {
      return this.lastTime;
   }

   @Override
   public LivingEntity getLivingEntity() {
      return this.livingEntity;
   }

   @Override
   public int getPriority() {
      return this.priority;
   }

   @Override
   public int getMaxDurationTicks() {
      return this.maxDurationTicks;
   }

   @Override
   public float getSpeed() {
      return this.speed;
   }

   @Override
   public AbstractWanderBackToPoint setSpeed(float speed) {
      this.speed = speed;
      return this;
   }

   @Override
   public Location getReturnLocation() {
      return this.returnLocation;
   }

   @Override
   public AbstractWanderBackToPoint setReturnLocation(Location location) {
      this.returnLocation = location.clone();
      return this;
   }

   @Override
   public int getStopReturnDistance() {
      return this.stopReturnDistance;
   }

   @Override
   public AbstractWanderBackToPoint setStopReturnDistance(int distance) {
      this.stopReturnDistance = distance;
      return this;
   }

   @Override
   public int getGoalRefreshCooldownTicks() {
      return this.goalRefreshCooldownTicks;
   }

   @Override
   public AbstractWanderBackToPoint setGoalRefreshCooldownTicks(int ticks) {
      this.goalRefreshCooldownTicks = ticks;
      return this;
   }

   @Override
   public boolean isHardObjective() {
      return this.hardObjective;
   }

   @Override
   public AbstractWanderBackToPoint setHardObjective(boolean hardObjective) {
      this.priority = -1;
      this.hardObjective = hardObjective;
      return this;
   }

   @Override
   public boolean isReturnDuringCombat() {
      return this.returnDuringCombat;
   }

   @Override
   public AbstractWanderBackToPoint setReturnDuringCombat(boolean returnDuringCombat) {
      if (returnDuringCombat) {
         this.priority = -1;
      }

      this.returnDuringCombat = returnDuringCombat;
      return this;
   }

   @Override
   public boolean isTeleportOnFail() {
      return this.teleportOnFail;
   }

   @Override
   public AbstractWanderBackToPoint setTeleportOnFail(boolean teleportOnFail) {
      this.teleportOnFail = teleportOnFail;
      return this;
   }

   @Override
   public boolean isStartWithCooldown() {
      return this.startWithCooldown;
   }

   @Override
   public AbstractWanderBackToPoint setStartWithCooldown(boolean startWithCooldown) {
      this.startWithCooldown = startWithCooldown;
      return this;
   }

   @Override
   public void updateCooldown() {
      this.lastTime = System.currentTimeMillis();
   }

   @Override
   public void register() {
      if (this.startWithCooldown) {
         this.updateCooldown();
      }

      GoalSelectorAccess.get(this.mob).addGoal(this.priority, this);
   }

   @Override
   public void unregister() {
      GoalSelectorAccess.get(this.mob).removeGoal(this);
   }
}

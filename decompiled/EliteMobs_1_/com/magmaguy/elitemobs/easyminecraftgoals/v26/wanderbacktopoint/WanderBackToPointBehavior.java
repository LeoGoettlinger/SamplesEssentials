package com.magmaguy.elitemobs.easyminecraftgoals.v26.wanderbacktopoint;

import com.google.common.collect.ImmutableList;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.WanderBackToPointState;
import com.magmaguy.elitemobs.easyminecraftgoals.utils.Utils;
import com.mojang.datafixers.util.Pair;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.Behavior.Status;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class WanderBackToPointBehavior extends Behavior<LivingEntity> implements AbstractWanderBackToPoint {
   private static final Field BEHAVIORS = behaviorField();
   private Location returnLocation;
   private final double maximumDistanceFromPoint;
   private final org.bukkit.entity.LivingEntity livingEntity;
   private final Mob mob;
   private final int maxDurationTicks;
   private final WanderBackToPointState returnState = new WanderBackToPointState(this);
   private long lastTime;
   private int priority;
   private float speed;
   private int stopReturnDistance = 0;
   private int goalRefreshCooldownTicks = 60;
   private boolean hardObjective = false;
   private boolean returnDuringCombat = false;
   private boolean teleportOnFail = false;
   private boolean startWithCooldown = false;
   private Path path = null;
   private Brain<?> registeredBrain;
   private int registeredPriority;
   private BukkitTask returnTask;

   public WanderBackToPointBehavior(
      org.bukkit.entity.LivingEntity livingEntity, Mob mob, Location location, double maximumDistanceFromPoint, int priority, int maxDurationTicks
   ) {
      super(Map.of(MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED), maxDurationTicks, maxDurationTicks);
      this.livingEntity = livingEntity;
      this.mob = mob;
      this.returnLocation = location;
      this.maximumDistanceFromPoint = maximumDistanceFromPoint;
      this.maxDurationTicks = maxDurationTicks;
      this.priority = priority;
      this.lastTime = 0L;
   }

   protected boolean checkExtraStartConditions(ServerLevel var0, LivingEntity nmsLivingEntity) {
      if (this.registeredBrain == null) {
         return false;
      } else if (!this.hardObjective && !this.returnDuringCombat && this.mob.getTarget() instanceof Player) {
         this.updateCooldown();
         return false;
      } else if (this.lastTime + 50L * this.goalRefreshCooldownTicks - System.currentTimeMillis() > 0L) {
         return false;
      } else {
         this.updateCooldown();
         if (Utils.distanceShorterThan(this.returnLocation.toVector(), this.livingEntity.getLocation().toVector(), this.maximumDistanceFromPoint)) {
            return false;
         } else {
            this.path = ((PathfinderMob)nmsLivingEntity)
               .getNavigation()
               .createPath(this.returnLocation.getX(), this.returnLocation.getY(), this.returnLocation.getZ(), this.stopReturnDistance);
            if (!this.returnState.begin()) {
               this.path = null;
               return false;
            } else if (this.registeredBrain == null) {
               this.path = null;
               this.returnState.end(false);
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

   protected void start(ServerLevel var0, LivingEntity var1, long var2) {
      if (this.registeredBrain != null && this.returnState.isActive() && this.path != null) {
         this.cancelReturnTask();
         this.mob.getNavigation().stop();
         if (!this.mob.getNavigation().moveTo(this.path, this.speed)) {
            this.returnState.end(true);
         } else {
            this.mob.getBrain().setActiveActivityIfPossible(Activity.CORE);
            if (this.hardObjective || this.returnDuringCombat) {
               this.returnTask = (new BukkitRunnable() {
                     public void run() {
                        if (!WanderBackToPointBehavior.this.returnState.isActive()) {
                           this.cancel();
                        } else if (!WanderBackToPointBehavior.this.livingEntity.isValid() || WanderBackToPointBehavior.this.livingEntity.isDead()) {
                           WanderBackToPointBehavior.this.returnState.end(false);
                           this.cancel();
                        } else if (!WanderBackToPointBehavior.this.returnState.hasTimedOut()
                           && WanderBackToPointBehavior.this.path != null
                           && WanderBackToPointBehavior.this.path.canReach()
                           && !WanderBackToPointBehavior.this.mob.getNavigation().isDone()) {
                           WanderBackToPointBehavior.this.mob.getNavigation().moveTo(WanderBackToPointBehavior.this.path, WanderBackToPointBehavior.this.speed);
                        } else {
                           WanderBackToPointBehavior.this.mob.getNavigation().stop();
                           WanderBackToPointBehavior.this.returnState.end(!WanderBackToPointBehavior.this.returnState.isAtReturnPoint());
                           this.cancel();
                        }
                     }
                  })
                  .runTaskTimer(NMSManager.pluginProvider, 0L, 1L);
            }
         }
      }
   }

   protected void stop(ServerLevel var0, LivingEntity var1, long var2) {
      this.cancelReturnTask();
      boolean teleportOnFailure = this.registeredBrain != null && this.returnState.isActive() && !this.returnState.isAtReturnPoint();
      this.mob.getNavigation().stop();
      this.path = null;
      this.returnState.end(teleportOnFailure);
      this.updateCooldown();
      this.mob.setAggressive(true);
   }

   protected boolean canStillUse(ServerLevel var0, LivingEntity var1, long var2) {
      if (this.registeredBrain == null || !this.returnState.isActive() || !this.livingEntity.isValid() || this.livingEntity.isDead()) {
         return false;
      } else if (this.returnState.hasTimedOut() || this.path == null || !this.path.canReach()) {
         return false;
      } else {
         return !this.hardObjective && !this.returnDuringCombat && this.mob.getTarget() instanceof Player
            ? false
            : !this.path.isDone() && !this.mob.getNavigation().isDone();
      }
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
   public org.bukkit.entity.LivingEntity getLivingEntity() {
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
      if (this.registeredBrain == null) {
         if (this.startWithCooldown) {
            this.updateCooldown();
         }

         this.mob.getBrain().addActivity(Activity.CORE, ImmutableList.of(Pair.of(this.priority, this)), Set.of(), Set.of());
         this.registeredBrain = this.mob.getBrain();
         this.registeredPriority = this.priority;
      }
   }

   @Override
   public void unregister() {
      Brain<?> brain = this.registeredBrain;
      if (brain != null) {
         this.registeredBrain = null;
         this.removeBehavior(brain);
         this.cancelReturnTask();
         if (this.getStatus() == Status.RUNNING) {
            ServerLevel level = (ServerLevel)this.mob.level();
            this.doStop(level, this.mob, level.getGameTime());
         } else {
            if (this.returnState.isActive()) {
               this.mob.getNavigation().stop();
            }

            this.path = null;
            this.returnState.end(false);
         }
      }
   }

   private void cancelReturnTask() {
      if (this.returnTask != null) {
         this.returnTask.cancel();
         this.returnTask = null;
      }
   }

   private static Field behaviorField() {
      try {
         Field field = Brain.class.getDeclaredField("availableBehaviorsByPriority");
         field.setAccessible(true);
         return field;
      } catch (ReflectiveOperationException var1) {
         throw new ExceptionInInitializerError(var1);
      }
   }

   private void removeBehavior(Brain<?> brain) {
      try {
         Map<Integer, Map<Activity, Set<BehaviorControl<?>>>> priorities = (Map<Integer, Map<Activity, Set<BehaviorControl<?>>>>)BEHAVIORS.get(brain);
         Map<Activity, Set<BehaviorControl<?>>> activities = priorities.get(this.registeredPriority);
         if (activities != null && activities.get(Activity.CORE) != null) {
            Set<BehaviorControl<?>> remaining = new HashSet<>(activities.get(Activity.CORE));
            if (remaining.remove(this)) {
               Map<Activity, Set<BehaviorControl<?>>> replacement = new HashMap<>(activities);
               if (remaining.isEmpty()) {
                  replacement.remove(Activity.CORE);
               } else {
                  replacement.put(Activity.CORE, remaining);
               }

               priorities.put(this.registeredPriority, replacement);
            }
         }
      } catch (IllegalAccessException var6) {
         throw new IllegalStateException("Could not retire native leash behavior", var6);
      }
   }
}

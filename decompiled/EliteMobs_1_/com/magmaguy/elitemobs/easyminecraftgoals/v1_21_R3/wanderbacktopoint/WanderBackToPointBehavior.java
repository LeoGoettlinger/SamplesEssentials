package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R3.wanderbacktopoint;

import com.google.common.collect.ImmutableList;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.AbstractWanderBackToPoint;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.WanderBackToPointState;
import com.magmaguy.elitemobs.easyminecraftgoals.utils.Utils;
import java.util.Map;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EntityLiving;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.pathfinder.PathEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class WanderBackToPointBehavior extends Behavior<EntityLiving> implements AbstractWanderBackToPoint {
   private Location returnLocation;
   private final double maximumDistanceFromPoint;
   private final LivingEntity livingEntity;
   private final EntityInsentient mob;
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
   private PathEntity path = null;

   public WanderBackToPointBehavior(
      LivingEntity livingEntity, EntityInsentient mob, Location location, double maximumDistanceFromPoint, int priority, int maxDurationTicks
   ) {
      super(Map.of(MemoryModuleType.m, MemoryStatus.c), maxDurationTicks, maxDurationTicks);
      this.livingEntity = livingEntity;
      this.mob = mob;
      this.returnLocation = location;
      this.maximumDistanceFromPoint = maximumDistanceFromPoint;
      this.maxDurationTicks = maxDurationTicks;
      this.priority = priority;
      this.lastTime = 0L;
   }

   protected boolean a(WorldServer var0, EntityLiving nmsLivingEntity) {
      if (!this.hardObjective && !this.returnDuringCombat && this.mob.O_() instanceof EntityHuman) {
         this.updateCooldown();
         return false;
      } else if (this.lastTime + 50L * this.goalRefreshCooldownTicks - System.currentTimeMillis() > 0L) {
         return false;
      } else {
         this.updateCooldown();
         if (Utils.distanceShorterThan(this.returnLocation.toVector(), this.livingEntity.getLocation().toVector(), this.maximumDistanceFromPoint)) {
            return false;
         } else {
            this.path = ((EntityCreature)nmsLivingEntity)
               .P()
               .a(this.returnLocation.getX(), this.returnLocation.getY(), this.returnLocation.getZ(), this.stopReturnDistance);
            if (!this.returnState.begin()) {
               this.path = null;
               return false;
            } else if (this.path != null && this.path.j()) {
               return true;
            } else {
               this.path = null;
               this.returnState.end(true);
               return false;
            }
         }
      }
   }

   protected void d(WorldServer var0, EntityLiving var1, long var2) {
      if (this.returnState.isActive() && this.path != null) {
         this.mob.P().m();
         if (!this.mob.P().a(this.path, this.speed)) {
            this.returnState.end(true);
         } else {
            this.mob.eb().a(Activity.a);
            if (this.hardObjective || this.returnDuringCombat) {
               (new BukkitRunnable() {
                     public void run() {
                        if (!WanderBackToPointBehavior.this.returnState.isActive()) {
                           this.cancel();
                        } else if (!WanderBackToPointBehavior.this.livingEntity.isValid() || WanderBackToPointBehavior.this.livingEntity.isDead()) {
                           WanderBackToPointBehavior.this.returnState.end(false);
                           this.cancel();
                        } else if (!WanderBackToPointBehavior.this.returnState.hasTimedOut()
                           && WanderBackToPointBehavior.this.path != null
                           && WanderBackToPointBehavior.this.path.j()
                           && !WanderBackToPointBehavior.this.mob.P().k()) {
                           WanderBackToPointBehavior.this.mob.P().a(WanderBackToPointBehavior.this.path, WanderBackToPointBehavior.this.speed);
                        } else {
                           WanderBackToPointBehavior.this.mob.P().m();
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

   protected void b(WorldServer var0, EntityLiving var1, long var2) {
      boolean teleportOnFailure = this.returnState.isActive() && !this.returnState.isAtReturnPoint();
      this.mob.P().m();
      this.path = null;
      this.returnState.end(teleportOnFailure);
      this.updateCooldown();
      this.mob.w(true);
   }

   protected boolean a(WorldServer var0, EntityLiving var1, long var2) {
      if (!this.returnState.isActive() || !this.livingEntity.isValid() || this.livingEntity.isDead()) {
         return false;
      } else if (this.returnState.hasTimedOut() || this.path == null || !this.path.j()) {
         return false;
      } else {
         return !this.hardObjective && !this.returnDuringCombat && this.mob.O_() instanceof EntityHuman ? false : !this.path.c() && !this.mob.P().k();
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

      this.mob.eb().a(Activity.a, this.priority, ImmutableList.of(this));
   }

   @Override
   public void unregister() {
   }
}

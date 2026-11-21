package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R4.pathfinding;

import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingHandle;
import com.magmaguy.elitemobs.easyminecraftgoals.PathfindingStatus;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.PathfindingDriver;
import com.magmaguy.elitemobs.easyminecraftgoals.internal.pathfinding.PathfindingSession;
import java.util.EnumSet;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.ai.goal.PathfinderGoal;
import net.minecraft.world.entity.ai.goal.PathfinderGoal.Type;
import net.minecraft.world.level.pathfinder.PathEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public final class NativePathfindingGoal extends PathfinderGoal implements PathfindingHandle, PathfindingDriver {
   private final EntityCreature mob;
   private final int priority;
   private final PathfindingSession session;
   private PathEntity path;

   public NativePathfindingGoal(EntityCreature mob, LivingEntity livingEntity, int priority) {
      this.mob = mob;
      this.priority = priority;
      this.session = new PathfindingSession(livingEntity, this);
      this.a(EnumSet.of(Type.a, Type.c));
   }

   public void register() {
      this.mob.bF.a(this.priority, this);
   }

   public boolean b() {
      return this.session.canUse();
   }

   public boolean c() {
      return this.session.canContinueToUse();
   }

   public void d() {
      this.session.start();
   }

   public void a() {
      this.session.tick();
   }

   public void e() {
      this.session.stop();
   }

   public boolean V_() {
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
      return this.mob.bJ() && !this.mob.dQ() && !this.mob.gj();
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
      return this.path != null && this.mob.N().a(this.path, speedModifier);
   }

   @Override
   public boolean navigationDone() {
      return this.mob.N().k();
   }

   @Override
   public void stopNavigation() {
      this.mob.N().m();
      this.path = null;
   }

   @Override
   public void unregisterGoal() {
      this.mob.bF.a(this);
   }
}

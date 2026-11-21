package com.magmaguy.elitemobs.easyminecraftgoals.v1_21_R5.move;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityInsentient;
import net.minecraft.world.entity.EnumMoveType;
import net.minecraft.world.entity.ai.attributes.GenericAttributes;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Move {
   public static boolean canReach(EntityCreature pathfinderMob, Location destination) {
      if (pathfinderMob.S() == null) {
         return true;
      } else {
         PathEntity path = pathfinderMob.S().a(destination.getX(), destination.getY(), destination.getZ(), 0);
         return path == null ? true : path.j();
      }
   }

   public static boolean simpleMove(EntityCreature pathfinderMob, double speedModifier, Location destination) {
      PathEntity path = pathfinderMob.S().a(destination.getX(), destination.getY(), destination.getZ(), 0);
      return pathfinderMob.S().a(path, speedModifier);
   }

   public static void doNotMove(EntityCreature pathfinderMob) {
      pathfinderMob.S().a(pathfinderMob, 0.0);
   }

   public static void universalMove(EntityInsentient mob, double speedModifier, Location destination) {
      double speed = mob.i(GenericAttributes.w) * 0.75;
      Vec3D movementInTick = new Vec3D(destination.getX(), destination.getY(), destination.getZ()).d(mob.dv());
      movementInTick = movementInTick.d().d(speedModifier * speed, speedModifier * speed, speedModifier * speed);
      mob.a(EnumMoveType.a, movementInTick);
      rotateHead(mob, destination.toVector(), new Vector(mob.dv().d, mob.dv().e, mob.dv().f));
   }

   private static void rotateHead(Entity entity, Vector destination, Vector currentLocation) {
      Vector newVector = destination.subtract(currentLocation);
      double x = newVector.getX();
      double z = newVector.getZ();
      double targetRot;
      if (Math.abs(x) > Math.abs(z)) {
         if (x > 0.0) {
            targetRot = -90.0;
         } else {
            targetRot = 90.0;
         }
      } else if (z > 0.0) {
         targetRot = 0.0;
      } else {
         targetRot = 180.0;
      }

      double currentRot = entity.dP();
      if (currentRot != targetRot) {
         if (targetRot - currentRot > 0.0) {
            entity.b(90.0, 0.0);
         } else {
            entity.b(-90.0, 0.0);
         }
      }
   }

   public static boolean forcedMove(EntityInsentient mob, double speedModifier, Location destination) {
      mob.gC();
      universalMove(mob, speedModifier, destination);
      return true;
   }
}

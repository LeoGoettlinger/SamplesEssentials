package com.magmaguy.elitemobs.magmacore.util;

import com.magmaguy.elitemobs.magmacore.location.LocationQueryRegistry;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public final class EntityMovementPath {
   private EntityMovementPath() {
   }

   public static boolean isClear(Entity entity, Location destination) {
      return inspect(entity, destination) == EntityMovementPath.Status.CLEAR;
   }

   public static EntityMovementPath.Status inspect(Entity entity, Location destination) {
      if (entity != null && entity.isValid() && !entity.isDead() && destination != null) {
         World world = entity.getWorld();
         if (!world.equals(destination.getWorld())) {
            return EntityMovementPath.Status.UNAVAILABLE;
         } else {
            Vector offset = destination.toVector().subtract(entity.getLocation().toVector());
            double distance = offset.length();
            if (!Double.isFinite(distance)) {
               return EntityMovementPath.Status.UNAVAILABLE;
            } else {
               BoundingBox source = entity.getBoundingBox();
               BoundingBox end = source.clone().shift(offset);
               if (!(Math.min(source.getMinY(), end.getMinY()) < world.getMinHeight())
                  && !(Math.max(source.getMaxY(), end.getMaxY()) > world.getMaxHeight())
                  && world.getWorldBorder().isInside(destination)) {
                  int steps = Math.max(1, (int)Math.ceil(distance * 4.0));
                  Vector step = offset.clone().multiply(1.0 / steps);
                  boolean checkProtection = !(entity instanceof Player);
                  BoundingBox previous = source;

                  for (int i = 1; i <= steps; i++) {
                     BoundingBox next = source.clone().shift(step.clone().multiply(i));
                     BoundingBox swept = previous.clone().union(next).expand(-1.0E-5);
                     EntityMovementPath.Status status = inspectVolume(world, swept, checkProtection);
                     if (status != EntityMovementPath.Status.CLEAR) {
                        return status;
                     }

                     previous = next;
                  }

                  return EntityMovementPath.Status.CLEAR;
               } else {
                  return EntityMovementPath.Status.UNAVAILABLE;
               }
            }
         }
      } else {
         return EntityMovementPath.Status.UNAVAILABLE;
      }
   }

   private static EntityMovementPath.Status inspectVolume(World world, BoundingBox volume, boolean checkProtection) {
      int minX = (int)Math.floor(volume.getMinX());
      int maxX = (int)Math.floor(volume.getMaxX());
      int minY = (int)Math.floor(volume.getMinY());
      int maxY = (int)Math.floor(volume.getMaxY());
      int minZ = (int)Math.floor(volume.getMinZ());
      int maxZ = (int)Math.floor(volume.getMaxZ());

      for (int x = minX; x <= maxX; x++) {
         for (int z = minZ; z <= maxZ; z++) {
            if (!world.isChunkLoaded(x >> 4, z >> 4)) {
               return EntityMovementPath.Status.UNAVAILABLE;
            }

            for (int y = Math.max(world.getMinHeight(), minY - 1); y <= maxY; y++) {
               Block block = world.getBlockAt(x, y, z);
               if (checkProtection && y >= minY && LocationQueryRegistry.isInAnyProtectedRegion(block.getLocation())) {
                  return EntityMovementPath.Status.PROTECTED;
               }

               if (!block.isPassable() && block.getBoundingBox().overlaps(volume)) {
                  return EntityMovementPath.Status.BLOCKED;
               }
            }
         }
      }

      return EntityMovementPath.Status.CLEAR;
   }

   public static enum Status {
      CLEAR,
      BLOCKED,
      PROTECTED,
      UNAVAILABLE;
   }
}

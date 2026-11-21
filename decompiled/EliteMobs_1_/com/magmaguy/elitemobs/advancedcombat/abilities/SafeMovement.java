package com.magmaguy.elitemobs.advancedcombat.abilities;

import java.util.EnumSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

final class SafeMovement {
   private static final double ROUTE_STEP = 0.125;
   private static final double BODY_EPSILON = 1.0E-4;
   private static final double CONTACT_EPSILON = 1.0E-7;
   private static final double SUPPORT_DEPTH = 0.075;
   private static final double SUPPORT_HALF_WIDTH = 0.12;
   private static final double STANDING_HALF_WIDTH = 0.3;
   private static final double STANDING_HEIGHT = 1.8;
   private static final Set<Material> UNSAFE_FOOTING = EnumSet.of(
      Material.LAVA,
      Material.FIRE,
      Material.SOUL_FIRE,
      Material.MAGMA_BLOCK,
      Material.CAMPFIRE,
      Material.SOUL_CAMPFIRE,
      Material.CACTUS,
      Material.SWEET_BERRY_BUSH,
      Material.WITHER_ROSE,
      Material.POWDER_SNOW,
      Material.POINTED_DRIPSTONE,
      Material.COBWEB
   );

   private SafeMovement() {
   }

   static double physicsDashClearDistance(Player player, double maximumDistance) {
      if (!validDistance(maximumDistance)) {
         return 0.0;
      } else {
         Vector direction = horizontalFacing(player);
         if (direction == null) {
            return 0.0;
         } else {
            Location start = player.getLocation();
            Location previous = start.clone();
            double lastClearDistance = 0.0;
            int steps = Math.max(1, (int)Math.ceil(maximumDistance / 0.125));

            for (int step = 1; step <= steps; step++) {
               double distance = Math.min(step * 0.125, maximumDistance);
               Location candidate = start.clone().add(direction.clone().multiply(distance));
               if (!bodyPathClear(player, previous, candidate)) {
                  break;
               }

               previous = candidate;
               lastClearDistance = distance;
            }

            return lastClearDistance;
         }
      }
   }

   static boolean safeStandingPlayerVolume(Player player, Location location) {
      BoundingBox body = standingBodyAt(player, location);
      return safeStanding(player, location, body, Math.max(0.12, body.getWidthX() * 0.5), Math.max(0.12, body.getWidthZ() * 0.5));
   }

   private static boolean safeStanding(Player player, Location location, BoundingBox body, double supportHalfWidthX, double supportHalfWidthZ) {
      World world = location.getWorld();
      if (world != null && world.equals(player.getWorld()) && volumeAvailable(player, world, body) && !intersectsCollision(world, body)) {
         BoundingBox supportProbe = new BoundingBox(
            body.getCenterX() - supportHalfWidthX,
            location.getY() - 0.075,
            body.getCenterZ() - supportHalfWidthZ,
            body.getCenterX() + supportHalfWidthX,
            location.getY() + 1.0E-4,
            body.getCenterZ() + supportHalfWidthZ
         );
         return intersectsCollision(world, supportProbe) && !containsUnsafeFooting(world, union(body, supportProbe));
      } else {
         return false;
      }
   }

   static boolean bodyPathClear(Player player, Location from, Location to) {
      return bodyPathClear(player, from, to, bodyAt(player, from), bodyAt(player, to));
   }

   private static boolean bodyPathClear(Player player, Location from, Location to, BoundingBox start, BoundingBox end) {
      World world = from.getWorld();
      if (world != null && to.getWorld() != null && world.equals(to.getWorld()) && world.equals(player.getWorld())) {
         BoundingBox swept = union(start, end);
         if (!volumeAvailable(player, world, swept)) {
            return false;
         } else if (!intersectsCollision(world, start) && !intersectsCollision(world, end)) {
            Vector movement = to.toVector().subtract(from.toVector());
            if (movement.lengthSquared() < 1.0E-12) {
               return true;
            } else {
               for (int x = minimumBlock(swept.getMinX()); x <= maximumBlock(swept.getMaxX()); x++) {
                  for (int y = minimumBlock(swept.getMinY()); y <= maximumBlock(swept.getMaxY()); y++) {
                     for (int z = minimumBlock(swept.getMinZ()); z <= maximumBlock(swept.getMaxZ()); z++) {
                        Block block = world.getBlockAt(x, y, z);

                        for (BoundingBox local : block.getCollisionShape().getBoundingBoxes()) {
                           BoundingBox obstacle = absolute(local, x, y, z);
                           if (swept.overlaps(obstacle) && sweptAabbIntersects(start, movement, obstacle)) {
                              return false;
                           }
                        }
                     }
                  }
               }

               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private static BoundingBox bodyAt(Player player, Location location) {
      Location current = player.getLocation();
      BoundingBox currentBox = player.getBoundingBox();
      double x = location.getX() - current.getX();
      double y = location.getY() - current.getY();
      double z = location.getZ() - current.getZ();
      return new BoundingBox(
         currentBox.getMinX() + x + 1.0E-4,
         currentBox.getMinY() + y + 1.0E-4,
         currentBox.getMinZ() + z + 1.0E-4,
         currentBox.getMaxX() + x - 1.0E-4,
         currentBox.getMaxY() + y - 1.0E-4,
         currentBox.getMaxZ() + z - 1.0E-4
      );
   }

   private static BoundingBox standingBodyAt(Player player, Location location) {
      BoundingBox currentBox = player.getBoundingBox();
      double halfWidthX = Math.max(0.3, currentBox.getWidthX() * 0.5);
      double halfWidthZ = Math.max(0.3, currentBox.getWidthZ() * 0.5);
      double scaleFromWidth = Math.max(halfWidthX, halfWidthZ) / 0.3;
      double height = Math.max(Math.max(1.8, currentBox.getHeight()), 1.8 * scaleFromWidth);
      return new BoundingBox(
         location.getX() - halfWidthX + 1.0E-4,
         location.getY() + 1.0E-4,
         location.getZ() - halfWidthZ + 1.0E-4,
         location.getX() + halfWidthX - 1.0E-4,
         location.getY() + height - 1.0E-4,
         location.getZ() + halfWidthZ - 1.0E-4
      );
   }

   static boolean volumeAvailable(Player player, World world, BoundingBox volume) {
      if (!(volume.getMinY() < world.getMinHeight()) && !(volume.getMaxY() >= world.getMaxHeight())) {
         for (int chunkX = minimumBlock(volume.getMinX()) >> 4; chunkX <= maximumBlock(volume.getMaxX()) >> 4; chunkX++) {
            for (int chunkZ = minimumBlock(volume.getMinZ()) >> 4; chunkZ <= maximumBlock(volume.getMaxZ()) >> 4; chunkZ++) {
               if (!world.isChunkLoaded(chunkX, chunkZ)) {
                  return false;
               }
            }
         }

         WorldBorder border = player.getWorldBorder();
         if (border == null) {
            border = world.getWorldBorder();
         }

         double y = Math.max((double)world.getMinHeight(), Math.min(world.getMaxHeight() - 1.0, volume.getMinY()));
         return border.isInside(new Location(world, volume.getMinX(), y, volume.getMinZ()))
            && border.isInside(new Location(world, volume.getMinX(), y, volume.getMaxZ()))
            && border.isInside(new Location(world, volume.getMaxX(), y, volume.getMinZ()))
            && border.isInside(new Location(world, volume.getMaxX(), y, volume.getMaxZ()));
      } else {
         return false;
      }
   }

   private static boolean intersectsCollision(World world, BoundingBox body) {
      for (int x = minimumBlock(body.getMinX()); x <= maximumBlock(body.getMaxX()); x++) {
         for (int y = minimumBlock(body.getMinY()); y <= maximumBlock(body.getMaxY()); y++) {
            for (int z = minimumBlock(body.getMinZ()); z <= maximumBlock(body.getMaxZ()); z++) {
               BoundingBox localBody = new BoundingBox(
                  body.getMinX() - x, body.getMinY() - y, body.getMinZ() - z, body.getMaxX() - x, body.getMaxY() - y, body.getMaxZ() - z
               );
               if (world.getBlockAt(x, y, z).getCollisionShape().overlaps(localBody)) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static boolean containsUnsafeFooting(World world, BoundingBox volume) {
      for (int x = minimumBlock(volume.getMinX()); x <= maximumBlock(volume.getMaxX()); x++) {
         for (int y = minimumBlock(volume.getMinY()); y <= maximumBlock(volume.getMaxY()); y++) {
            for (int z = minimumBlock(volume.getMinZ()); z <= maximumBlock(volume.getMaxZ()); z++) {
               Block block = world.getBlockAt(x, y, z);
               if (block.isLiquid() || UNSAFE_FOOTING.contains(block.getType())) {
                  return true;
               }
            }
         }
      }

      return false;
   }

   private static boolean sweptAabbIntersects(BoundingBox moving, Vector delta, BoundingBox obstacle) {
      SafeMovement.AxisTimes x = axisTimes(moving.getMinX(), moving.getMaxX(), delta.getX(), obstacle.getMinX(), obstacle.getMaxX());
      if (x == null) {
         return false;
      } else {
         SafeMovement.AxisTimes y = axisTimes(moving.getMinY(), moving.getMaxY(), delta.getY(), obstacle.getMinY(), obstacle.getMaxY());
         if (y == null) {
            return false;
         } else {
            SafeMovement.AxisTimes z = axisTimes(moving.getMinZ(), moving.getMaxZ(), delta.getZ(), obstacle.getMinZ(), obstacle.getMaxZ());
            if (z == null) {
               return false;
            } else {
               double entry = Math.max(x.entry(), Math.max(y.entry(), z.entry()));
               double exit = Math.min(x.exit(), Math.min(y.exit(), z.exit()));
               return entry < exit - 1.0E-7 && exit > 1.0E-7 && entry < 0.9999999;
            }
         }
      }
   }

   private static SafeMovement.AxisTimes axisTimes(double movingMin, double movingMax, double delta, double obstacleMin, double obstacleMax) {
      if (Math.abs(delta) < 1.0E-12) {
         return !(movingMax <= obstacleMin + 1.0E-7) && !(movingMin >= obstacleMax - 1.0E-7)
            ? new SafeMovement.AxisTimes(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)
            : null;
      } else {
         double first = (obstacleMin - movingMax) / delta;
         double second = (obstacleMax - movingMin) / delta;
         return new SafeMovement.AxisTimes(Math.min(first, second), Math.max(first, second));
      }
   }

   private static BoundingBox absolute(BoundingBox local, int x, int y, int z) {
      return new BoundingBox(local.getMinX() + x, local.getMinY() + y, local.getMinZ() + z, local.getMaxX() + x, local.getMaxY() + y, local.getMaxZ() + z);
   }

   private static BoundingBox union(BoundingBox first, BoundingBox second) {
      return new BoundingBox(
         Math.min(first.getMinX(), second.getMinX()),
         Math.min(first.getMinY(), second.getMinY()),
         Math.min(first.getMinZ(), second.getMinZ()),
         Math.max(first.getMaxX(), second.getMaxX()),
         Math.max(first.getMaxY(), second.getMaxY()),
         Math.max(first.getMaxZ(), second.getMaxZ())
      );
   }

   private static Vector horizontalFacing(Player player) {
      Vector direction = player.getEyeLocation().getDirection().setY(0);
      return direction.lengthSquared() < 1.0E-6 ? null : direction.normalize();
   }

   private static boolean validDistance(double distance) {
      return Double.isFinite(distance) && distance > 0.0;
   }

   private static int minimumBlock(double coordinate) {
      return (int)Math.floor(coordinate);
   }

   private static int maximumBlock(double coordinate) {
      return (int)Math.floor(coordinate - 1.0E-4);
   }

   private record AxisTimes(double entry, double exit) {
   }
}

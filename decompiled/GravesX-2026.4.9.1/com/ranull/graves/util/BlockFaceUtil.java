package com.ranull.graves.util;

import org.bukkit.Rotation;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;

public class BlockFaceUtil {
   private BlockFaceUtil() {
   }

   public static BlockFace getSimpleBlockFace(BlockFace blockFace) {
      return switch (blockFace) {
         case EAST, NORTH_EAST -> BlockFace.EAST;
         case SOUTH, SOUTH_EAST -> BlockFace.SOUTH;
         case WEST, SOUTH_WEST -> BlockFace.WEST;
         default -> BlockFace.NORTH;
      };
   }

   public static BlockFace getEntityYawBlockFace(LivingEntity livingEntity) {
      return getYawBlockFace(livingEntity.getLocation().getYaw());
   }

   public static Rotation getBlockFaceRotation(BlockFace blockFace) {
      return switch (blockFace) {
         case EAST -> Rotation.COUNTER_CLOCKWISE;
         case NORTH_EAST -> Rotation.FLIPPED_45;
         case SOUTH -> Rotation.NONE;
         case SOUTH_EAST -> Rotation.COUNTER_CLOCKWISE_45;
         case WEST -> Rotation.CLOCKWISE_45;
         case SOUTH_WEST -> Rotation.CLOCKWISE;
         case NORTH_WEST -> Rotation.CLOCKWISE_135;
         default -> Rotation.FLIPPED;
      };
   }

   public static BlockFace getYawBlockFace(float yaw) {
      float direction = yaw % 360.0F;
      if (direction < 0.0F) {
         direction += 360.0F;
      }
      return switch (Math.round(direction / 45.0F)) {
         case 1 -> BlockFace.SOUTH_WEST;
         case 2 -> BlockFace.WEST;
         case 3 -> BlockFace.NORTH_WEST;
         case 4 -> BlockFace.NORTH;
         case 5 -> BlockFace.NORTH_EAST;
         case 6 -> BlockFace.EAST;
         case 7 -> BlockFace.SOUTH_EAST;
         default -> BlockFace.SOUTH;
      };
   }

   public static int getBlockFaceYaw(BlockFace blockFace) {
      return switch (blockFace) {
         case EAST -> -90;
         case NORTH_EAST -> -135;
         case SOUTH -> 0;
         case SOUTH_EAST -> -45;
         case WEST -> 90;
         case SOUTH_WEST -> 45;
         case NORTH_WEST -> 135;
         default -> 180;
      };
   }
}

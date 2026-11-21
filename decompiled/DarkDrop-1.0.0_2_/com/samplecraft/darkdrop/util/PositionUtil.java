package com.samplecraft.darkdrop.util;

import com.samplecraft.darkdrop.DarkDrop;
import com.samplecraft.darkdrop.manager.ConfigManager;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;

public final class PositionUtil {
   private PositionUtil() {
   }

   public static Location findRandomSafeLocation(DarkDrop plugin, World world, Location center) {
      ConfigManager cfg = plugin.getConfigManager();
      int minDist = cfg.getRandomMinDistance();
      int maxDist = cfg.getRandomMaxDistance();
      int margin = cfg.getWorldBorderMargin();
      int minSafeY = cfg.getMinSafeY();
      int maxSafeY = cfg.getMaxSafeY();
      WorldBorder border = world.getWorldBorder();
      Location borderCenter = border.getCenter();
      double borderSize = border.getSize();
      double half = borderSize / 2.0 - margin;
      ThreadLocalRandom rnd = ThreadLocalRandom.current();

      for (int attempt = 0; attempt < 40; attempt++) {
         double angle = rnd.nextDouble(0.0, Math.PI * 2);
         double dist = rnd.nextDouble(minDist, maxDist);
         double dx = Math.cos(angle) * dist;
         double dz = Math.sin(angle) * dist;
         double targetX = center.getX() + dx;
         double targetZ = center.getZ() + dz;
         if (!(Math.abs(targetX - borderCenter.getX()) > half) && !(Math.abs(targetZ - borderCenter.getZ()) > half)) {
            int x = targetX < 0.0 ? (int)Math.floor(targetX) : (int)targetX;
            int z = targetZ < 0.0 ? (int)Math.floor(targetZ) : (int)targetZ;
            int topY = findTopSolidY(world, x, z, minSafeY, maxSafeY);
            if (topY != Integer.MIN_VALUE) {
               Block landing = world.getBlockAt(x, topY, z);
               if (isSafeLandingBlock(landing)) {
                  return new Location(world, x + 0.5, topY + 1, z + 0.5);
               }
            }
         }
      }

      return null;
   }

   private static int findTopSolidY(World world, int x, int z, int minSafeY, int maxSafeY) {
      for (int y = maxSafeY; y >= minSafeY; y--) {
         Block b = world.getBlockAt(x, y, z);
         if (b.getType().isSolid() && !b.getType().isAir()) {
            return y;
         }
      }

      return Integer.MIN_VALUE;
   }

   private static boolean isSafeLandingBlock(Block block) {
      Material mat = block.getType();
      if (mat.isAir()) {
         return false;
      } else {
         return switch (mat) {
            case LAVA, WATER, BUBBLE_COLUMN, MAGMA_BLOCK, FIRE, SOUL_FIRE, CACTUS, SWEET_BERRY_BUSH, WITHER_ROSE, POINTED_DRIPSTONE -> false;
            default -> true;
         };
      }
   }

   public static Double parseCoordinate(String token, double current) {
      if (token != null && !token.isEmpty()) {
         try {
            if (token.startsWith("~")) {
               String rest = token.substring(1);
               if (rest.isEmpty()) {
                  return current;
               } else {
                  double offset = Double.parseDouble(rest);
                  return current + offset;
               }
            } else {
               return Double.parseDouble(token);
            }
         } catch (NumberFormatException var6) {
            return null;
         }
      } else {
         return null;
      }
   }
}

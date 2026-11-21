package com.skyblockexp.ezrtp.teleport.search;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.World;
import org.bukkit.block.Biome;

public final class SquareSearchStrategy implements BiomeSearchStrategy {
   @Override
   public int[] generateCandidateCoordinates(
      World world, int centerX, int centerZ, int minRadius, int maxRadius, Set<Biome> targetBiomes, RareBiomeRegistry registry
   ) {
      int effectiveMin = Math.min(minRadius, maxRadius);
      int effectiveMax = Math.max(minRadius, maxRadius);
      if (effectiveMax <= 0 && effectiveMin <= 0) {
         return new int[]{centerX, centerZ};
      } else {
         int radius = ThreadLocalRandom.current().nextInt(effectiveMax - effectiveMin + 1) + effectiveMin;
         int edge = ThreadLocalRandom.current().nextInt(4);
         int offset = ThreadLocalRandom.current().nextInt(radius * 2 + 1);
         int localX = 0;
         int localZ = 0;
         switch (edge) {
            case 0:
               localX = -radius + offset;
               localZ = -radius;
               break;
            case 1:
               localX = radius;
               localZ = -radius + offset;
               break;
            case 2:
               localX = radius - offset;
               localZ = radius;
               break;
            case 3:
               localX = -radius;
               localZ = radius - offset;
         }

         return new int[]{centerX + localX, centerZ + localZ};
      }
   }

   @Override
   public String getStrategyName() {
      return "square";
   }

   @Override
   public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
      return true;
   }
}

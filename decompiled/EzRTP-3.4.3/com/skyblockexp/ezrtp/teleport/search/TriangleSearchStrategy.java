package com.skyblockexp.ezrtp.teleport.search;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.World;
import org.bukkit.block.Biome;

public final class TriangleSearchStrategy implements BiomeSearchStrategy {
   @Override
   public int[] generateCandidateCoordinates(
      World world, int centerX, int centerZ, int minRadius, int maxRadius, Set<Biome> targetBiomes, RareBiomeRegistry registry
   ) {
      int effectiveMin = Math.min(minRadius, maxRadius);
      int effectiveMax = Math.max(minRadius, maxRadius);
      if (effectiveMax <= 0 && effectiveMin <= 0) {
         return new int[]{centerX, centerZ};
      } else {
         int radius = effectiveMin == effectiveMax ? effectiveMin : SamplingUtil.randomIntInclusive(effectiveMin, effectiveMax);
         double[] vx = new double[3];
         double[] vz = new double[3];

         for (int i = 0; i < 3; i++) {
            double angle = (-Math.PI / 2) + i * 2.0 * Math.PI / 3.0;
            vx[i] = Math.cos(angle) * radius;
            vz[i] = Math.sin(angle) * radius;
         }

         int edge = ThreadLocalRandom.current().nextInt(3);
         int next = (edge + 1) % 3;
         double t = SamplingUtil.randomDouble();
         double px = (1.0 - t) * vx[edge] + t * vx[next];
         double pz = (1.0 - t) * vz[edge] + t * vz[next];
         int finalX = centerX + (int)Math.round(px);
         int finalZ = centerZ + (int)Math.round(pz);
         return new int[]{finalX, finalZ};
      }
   }

   @Override
   public String getStrategyName() {
      return "triangle";
   }

   @Override
   public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
      return true;
   }
}

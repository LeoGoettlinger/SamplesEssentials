package com.skyblockexp.ezrtp.teleport.search;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.World;
import org.bukkit.block.Biome;

public final class DiamondSearchStrategy implements BiomeSearchStrategy {
   private static final double[] UNIT_VERTS_X = new double[]{0.0, 0.5, 1.0, 0.5, 0.0, -0.5, -1.0, -0.5};
   private static final double[] UNIT_VERTS_Z = new double[]{-1.0, -0.5, 0.0, 0.5, 1.0, 0.5, 0.0, -0.5};
   private static final double UNIT_EDGE_LENGTH = Math.hypot(0.5, 0.5);

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
         if (radius <= 0) {
            return new int[]{centerX, centerZ};
         } else {
            int edgeSteps = Math.max(1, (int)Math.round(radius * UNIT_EDGE_LENGTH));
            int totalSteps = 8 * edgeSteps;
            int idx = ThreadLocalRandom.current().nextInt(totalSteps);
            int chosenEdge = idx / edgeSteps;
            int chosenStep = idx % edgeSteps;
            int nextEdge = (chosenEdge + 1) % 8;
            double x1 = UNIT_VERTS_X[chosenEdge];
            double z1 = UNIT_VERTS_Z[chosenEdge];
            double dx = UNIT_VERTS_X[nextEdge] - x1;
            double dz = UNIT_VERTS_Z[nextEdge] - z1;
            double t = (double)chosenStep / edgeSteps;
            int px = centerX + (int)Math.round((x1 + dx * t) * radius);
            int pz = centerZ + (int)Math.round((z1 + dz * t) * radius);
            return new int[]{px, pz};
         }
      }
   }

   @Override
   public String getStrategyName() {
      return "diamond";
   }

   @Override
   public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
      return true;
   }
}

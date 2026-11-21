package com.skyblockexp.ezrtp.teleport.search;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.block.Biome;

public final class CircularSearchStrategy implements BiomeSearchStrategy {
   @Override
   public int[] generateCandidateCoordinates(
      World world, int centerX, int centerZ, int minRadius, int maxRadius, Set<Biome> targetBiomes, RareBiomeRegistry registry
   ) {
      int effectiveMin = Math.min(minRadius, maxRadius);
      int effectiveMax = Math.max(minRadius, maxRadius);
      double radius = SamplingUtil.randomDouble() * (effectiveMax - effectiveMin) + effectiveMin;
      double angle = SamplingUtil.randomAngle();
      int x = centerX + (int)Math.round(Math.cos(angle) * radius);
      int z = centerZ + (int)Math.round(Math.sin(angle) * radius);
      return new int[]{x, z};
   }

   @Override
   public String getStrategyName() {
      return "circle";
   }

   @Override
   public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
      return true;
   }
}

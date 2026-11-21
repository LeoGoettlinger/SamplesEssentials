package com.skyblockexp.ezrtp.teleport.biome;

import com.skyblockexp.ezrtp.teleport.search.BiomeSearchStrategy;
import com.skyblockexp.ezrtp.teleport.search.SamplingUtil;
import com.skyblockexp.ezrtp.teleport.search.UniformSearchStrategy;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

public final class WeightedRareBiomeStrategy implements BiomeSearchStrategy {
   private static final double HOTSPOT_WEIGHT = 0.7;
   private static final int HOTSPOT_SEARCH_RADIUS = 512;
   private final BiomeSearchStrategy fallbackStrategy;

   public WeightedRareBiomeStrategy() {
      this(new UniformSearchStrategy());
   }

   public WeightedRareBiomeStrategy(BiomeSearchStrategy fallbackStrategy) {
      this.fallbackStrategy = (BiomeSearchStrategy)(fallbackStrategy != null ? fallbackStrategy : new UniformSearchStrategy());
   }

   @Override
   public int[] generateCandidateCoordinates(
      World world, int centerX, int centerZ, int minRadius, int maxRadius, Set<Biome> targetBiomes, RareBiomeRegistry registry
   ) {
      if (!this.hasRareBiomes(targetBiomes, registry)) {
         return this.fallbackStrategy.generateCandidateCoordinates(world, centerX, centerZ, minRadius, maxRadius, targetBiomes, registry);
      } else {
         if (ThreadLocalRandom.current().nextDouble() < 0.7) {
            for (Biome biome : targetBiomes) {
               if (registry.isRareBiome(biome)) {
                  List<Location> hotspots = registry.getHotspots(world, biome);
                  if (!hotspots.isEmpty()) {
                     Location hotspot = hotspots.get(ThreadLocalRandom.current().nextInt(hotspots.size()));
                     int offsetRadius = SamplingUtil.randomIntInclusive(0, 511);
                     double angle = SamplingUtil.randomAngle();
                     int x = hotspot.getBlockX() + (int)Math.round(Math.cos(angle) * offsetRadius);
                     int z = hotspot.getBlockZ() + (int)Math.round(Math.sin(angle) * offsetRadius);
                     double distFromCenter = Math.sqrt(Math.pow(x - centerX, 2.0) + Math.pow(z - centerZ, 2.0));
                     if (distFromCenter >= minRadius && distFromCenter <= maxRadius) {
                        return new int[]{x, z};
                     }
                  }
               }
            }
         }

         return this.fallbackStrategy.generateCandidateCoordinates(world, centerX, centerZ, minRadius, maxRadius, targetBiomes, registry);
      }
   }

   @Override
   public String getStrategyName() {
      return "weighted-rare-biome";
   }

   @Override
   public boolean isApplicableFor(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
      return this.hasRareBiomes(targetBiomes, registry);
   }

   private boolean hasRareBiomes(Set<Biome> targetBiomes, RareBiomeRegistry registry) {
      return targetBiomes != null && !targetBiomes.isEmpty() && registry != null ? targetBiomes.stream().anyMatch(registry::isRareBiome) : false;
   }
}

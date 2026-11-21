package com.skyblockexp.ezrtp.teleport.search;

import com.skyblockexp.ezrtp.teleport.biome.RareBiomeRegistry;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.block.Biome;

public interface BiomeSearchStrategy {
   int[] generateCandidateCoordinates(World var1, int var2, int var3, int var4, int var5, Set<Biome> var6, RareBiomeRegistry var7);

   String getStrategyName();

   boolean isApplicableFor(Set<Biome> var1, RareBiomeRegistry var2);
}

package com.sk89q.worldedit.world.biome;

import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.util.WeightedChoice;
import com.sk89q.worldedit.util.function.LevenshteinDistance;
import com.sk89q.worldedit.world.registry.BiomeRegistry;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;

@Deprecated
public final class Biomes {
   private Biomes() {
   }

   @Deprecated
   @Nullable
   public static BiomeType findBiomeByName(Collection<BiomeType> biomes, String name, BiomeRegistry registry) {
      Preconditions.checkNotNull(biomes);
      Preconditions.checkNotNull(name);
      Preconditions.checkNotNull(registry);
      Function<String, ? extends Number> compare = new LevenshteinDistance(name, false, LevenshteinDistance.STANDARD_CHARS);
      WeightedChoice<BiomeType> chooser = new WeightedChoice<>(Functions.compose(compare::apply, new BiomeName(registry)), 0.0);

      for (BiomeType biome : biomes) {
         chooser.consider(biome);
      }

      Optional<WeightedChoice.Choice<BiomeType>> choice = chooser.getChoice();
      return choice.isPresent() && choice.get().getScore() <= 1.0 ? choice.get().getValue() : null;
   }
}

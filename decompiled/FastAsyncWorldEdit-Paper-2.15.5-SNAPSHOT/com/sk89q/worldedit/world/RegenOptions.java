package com.sk89q.worldedit.world;

import com.google.auto.value.AutoValue;
import com.sk89q.worldedit.world.biome.BiomeType;
import java.util.OptionalLong;
import javax.annotation.Nullable;

@AutoValue
public abstract class RegenOptions {
   public static RegenOptions.Builder builder() {
      return new AutoValue_RegenOptions.Builder().seed(OptionalLong.empty()).regenBiomes(false).biomeType(null);
   }

   RegenOptions() {
   }

   public abstract OptionalLong getSeed();

   abstract boolean isRegenBiomes();

   public final boolean shouldRegenBiomes() {
      return this.isRegenBiomes();
   }

   @Nullable
   public abstract BiomeType getBiomeType();

   public boolean hasBiomeType() {
      return this.getBiomeType() != null;
   }

   @com.google.auto.value.AutoValue.Builder
   public abstract static class Builder {
      public final RegenOptions.Builder seed(@Nullable Long seed) {
         return this.seed(seed == null ? OptionalLong.empty() : OptionalLong.of(seed));
      }

      abstract RegenOptions.Builder seed(OptionalLong seed);

      public abstract RegenOptions.Builder regenBiomes(boolean regenBiomes);

      public abstract RegenOptions.Builder biomeType(@Nullable BiomeType biomeType);

      public abstract RegenOptions build();
   }
}

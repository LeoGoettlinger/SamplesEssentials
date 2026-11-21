package com.sk89q.worldedit.world;

import com.sk89q.worldedit.world.biome.BiomeType;
import java.util.OptionalLong;
import javax.annotation.Nullable;

final class AutoValue_RegenOptions extends RegenOptions {
   private final OptionalLong seed;
   private final boolean regenBiomes;
   @Nullable
   private final BiomeType biomeType;

   private AutoValue_RegenOptions(OptionalLong seed, boolean regenBiomes, @Nullable BiomeType biomeType) {
      this.seed = seed;
      this.regenBiomes = regenBiomes;
      this.biomeType = biomeType;
   }

   @Override
   public OptionalLong getSeed() {
      return this.seed;
   }

   @Override
   boolean isRegenBiomes() {
      return this.regenBiomes;
   }

   @Nullable
   @Override
   public BiomeType getBiomeType() {
      return this.biomeType;
   }

   @Override
   public String toString() {
      return "RegenOptions{seed=" + this.seed + ", regenBiomes=" + this.regenBiomes + ", biomeType=" + this.biomeType + "}";
   }

   @Override
   public boolean equals(@org.jspecify.annotations.Nullable Object o) {
      if (o == this) {
         return true;
      } else {
         return !(o instanceof RegenOptions that)
            ? false
            : this.seed.equals(that.getSeed())
               && this.regenBiomes == that.isRegenBiomes()
               && (this.biomeType == null ? that.getBiomeType() == null : this.biomeType.equals(that.getBiomeType()));
      }
   }

   @Override
   public int hashCode() {
      int h$ = 1;
      h$ *= 1000003;
      h$ ^= this.seed.hashCode();
      h$ *= 1000003;
      h$ ^= this.regenBiomes ? 1231 : 1237;
      h$ *= 1000003;
      return h$ ^ (this.biomeType == null ? 0 : this.biomeType.hashCode());
   }

   static final class Builder extends RegenOptions.Builder {
      private OptionalLong seed = OptionalLong.empty();
      private boolean regenBiomes;
      private BiomeType biomeType;
      private byte set$0;

      @Override
      RegenOptions.Builder seed(OptionalLong seed) {
         if (seed == null) {
            throw new NullPointerException("Null seed");
         } else {
            this.seed = seed;
            return this;
         }
      }

      @Override
      public RegenOptions.Builder regenBiomes(boolean regenBiomes) {
         this.regenBiomes = regenBiomes;
         this.set$0 = (byte)(this.set$0 | 1);
         return this;
      }

      @Override
      public RegenOptions.Builder biomeType(@Nullable BiomeType biomeType) {
         this.biomeType = biomeType;
         return this;
      }

      @Override
      public RegenOptions build() {
         if (this.set$0 != 1) {
            String missing = " regenBiomes";
            throw new IllegalStateException("Missing required properties:" + missing);
         } else {
            return new AutoValue_RegenOptions(this.seed, this.regenBiomes, this.biomeType);
         }
      }
   }
}

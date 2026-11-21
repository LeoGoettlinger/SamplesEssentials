package com.sk89q.worldedit.function.mask;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import java.util.Arrays;
import java.util.Collection;
import javax.annotation.Nullable;

public class BiomeMask extends AbstractExtentMask {
   private final boolean[] biomes;

   public BiomeMask(Extent extent, Collection<BiomeType> biomes) {
      super(extent);
      Preconditions.checkNotNull(biomes);
      this.biomes = new boolean[BiomeType.REGISTRY.size()];

      for (BiomeType biome : biomes) {
         this.biomes[biome.getInternalId()] = true;
      }
   }

   private BiomeMask(Extent extent, boolean[] biomes) {
      super(extent);
      this.biomes = biomes;
   }

   public BiomeMask(Extent extent, BiomeType... biome) {
      this(extent, Arrays.asList((BiomeType[])Preconditions.checkNotNull(biome)));
   }

   public void add(Collection<BiomeType> biomes) {
      Preconditions.checkNotNull(biomes);

      for (BiomeType biome : biomes) {
         this.biomes[biome.getInternalId()] = true;
      }
   }

   public void add(BiomeType... biome) {
      this.add(Arrays.asList((BiomeType[])Preconditions.checkNotNull(biome)));
   }

   public Collection<BiomeType> getBiomes() {
      return BiomeType.REGISTRY.values().stream().filter(type -> this.biomes[type.getInternalId()]).toList();
   }

   @Override
   public boolean test(BlockVector3 vector) {
      BiomeType biome = vector.getBiome(this.getExtent());
      return this.biomes[biome.getInternalId()];
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      return null;
   }

   @Override
   public Mask copy() {
      return new BiomeMask(this.getExtent(), (boolean[])this.biomes.clone());
   }

   @Override
   public boolean test(Extent extent, BlockVector3 position) {
      BiomeType biome = this.getExtent().getBiome(position);
      return this.biomes[biome.getInternalId()];
   }
}

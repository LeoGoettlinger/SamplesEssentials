package com.sk89q.worldedit.extent.buffer;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.pattern.BiomePattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.AbstractFlatRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ForgetfulExtentBuffer extends AbstractDelegateExtent implements Pattern, BiomePattern {
   private final Map<BlockVector3, BaseBlock> buffer = new LinkedHashMap<>();
   private final Map<BlockVector3, BiomeType> biomeBuffer = new LinkedHashMap<>();
   private final Mask mask;
   private BlockVector3 min = null;
   private BlockVector3 max = null;

   public ForgetfulExtentBuffer(Extent delegate) {
      this(delegate, Masks.alwaysTrue());
   }

   @Override
   public boolean isQueueEnabled() {
      return true;
   }

   public ForgetfulExtentBuffer(Extent delegate, Mask mask) {
      super(delegate);
      Preconditions.checkNotNull(mask);
      this.mask = mask;
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
      if (this.min == null) {
         this.min = location;
      } else {
         this.min = this.min.getMinimum(location);
      }

      if (this.max == null) {
         this.max = location;
      } else {
         this.max = this.max.getMaximum(location);
      }

      if (this.mask.test(location)) {
         this.buffer.put(location, block.toBaseBlock());
         return true;
      } else {
         return this.getExtent().setBlock(location, block);
      }
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      if (this.min == null) {
         this.min = position;
      } else {
         this.min = this.min.getMinimum(position);
      }

      if (this.max == null) {
         this.max = position;
      } else {
         this.max = this.max.getMaximum(position);
      }

      if (this.mask.test(position)) {
         this.biomeBuffer.put(position, biome);
         return true;
      } else {
         return this.getExtent().setBiome(position, biome);
      }
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      if (this.min == null) {
         this.min = BlockVector3.at(x, y, z);
      } else {
         this.min = this.min.getMinimum(BlockVector3.at(x, y, z));
      }

      if (this.max == null) {
         this.max = BlockVector3.at(x, y, z);
      } else {
         this.max = this.max.getMaximum(BlockVector3.at(x, y, z));
      }

      BlockVector3 pos = BlockVector3.at(x, y, z);
      if (this.mask.test(pos)) {
         this.biomeBuffer.put(pos, biome);
         return true;
      } else {
         return this.getExtent().setBiome(x, y, z, biome);
      }
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 pos) {
      BaseBlock block = this.buffer.get(pos);
      return block != null ? block : BlockTypes.AIR.getDefaultState().toBaseBlock();
   }

   @Override
   public BiomeType applyBiome(BlockVector3 pos) {
      BiomeType biome = this.biomeBuffer.get(pos);
      return biome != null ? biome : BiomeTypes.OCEAN;
   }

   public Region asRegion() {
      return new AbstractFlatRegion(null) {
         @Override
         public BlockVector3 getMinimumPoint() {
            return ForgetfulExtentBuffer.this.min != null ? ForgetfulExtentBuffer.this.min : BlockVector3.ZERO;
         }

         @Override
         public BlockVector3 getMaximumPoint() {
            return ForgetfulExtentBuffer.this.max != null ? ForgetfulExtentBuffer.this.max : BlockVector3.ZERO;
         }

         @Override
         public void expand(BlockVector3... changes) throws RegionOperationException {
            throw new UnsupportedOperationException("Cannot change the size of this region");
         }

         @Override
         public void contract(BlockVector3... changes) throws RegionOperationException {
            throw new UnsupportedOperationException("Cannot change the size of this region");
         }

         @Override
         public boolean contains(BlockVector3 position) {
            return ForgetfulExtentBuffer.this.buffer.containsKey(position);
         }

         @Override
         public Iterator<BlockVector3> iterator() {
            return ForgetfulExtentBuffer.this.buffer.keySet().iterator();
         }

         @Override
         public Iterable<BlockVector2> asFlatRegion() {
            return ForgetfulExtentBuffer.this.biomeBuffer.keySet().stream().map(BlockVector3::toBlockVector2).collect(Collectors.toSet());
         }
      };
   }

   @Override
   public Pattern fork() {
      return new ForgetfulExtentBuffer(this.extent, this.mask.copy());
   }
}

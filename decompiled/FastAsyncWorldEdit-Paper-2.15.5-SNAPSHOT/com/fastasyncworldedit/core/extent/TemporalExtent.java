package com.fastasyncworldedit.core.extent;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

public class TemporalExtent extends PassthroughExtent {
   private int x;
   private int y;
   private int z = Integer.MAX_VALUE;
   private BlockStateHolder<?> block = BlockTypes.AIR.getDefaultState();
   private int bx;
   private int bz = Integer.MAX_VALUE;
   private BiomeType biome = null;

   public TemporalExtent(Extent extent) {
      super(extent);
   }

   public <B extends BlockStateHolder<B>> void set(int x, int y, int z, B block) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.block = block;
   }

   public void set(int x, int z, BiomeType biome) {
      this.bx = x;
      this.bz = z;
      this.biome = biome;
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      return position.x() == this.x && position.y() == this.y && position.z() == this.z ? this.block.toImmutableState() : super.getBlock(position);
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.x == x && this.y == y && this.z == z ? this.block.toImmutableState() : super.getBlock(x, y, z);
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      if (position.x() != this.x || position.y() != this.y || position.z() != this.z) {
         return super.getFullBlock(position);
      } else {
         return this.block instanceof BaseBlock ? (BaseBlock)this.block : this.block.toBaseBlock();
      }
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return position.x() == this.bx && position.z() == this.bz ? this.biome : super.getBiome(position);
   }
}

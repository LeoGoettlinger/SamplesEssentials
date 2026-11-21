package com.fastasyncworldedit.core.extent.filter.block;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import javax.annotation.Nullable;

public abstract class AbstractSingleFilterBlock extends FilterBlock {
   private BaseBlock block;
   private BiomeType biome;

   public AbstractSingleFilterBlock init(BaseBlock block) {
      this.block = block;
      return this;
   }

   public AbstractSingleFilterBlock init(BiomeType type) {
      this.biome = type;
      return this;
   }

   @Override
   public Extent getExtent() {
      return this;
   }

   @Override
   public int getOrdinal() {
      return this.block.getOrdinal();
   }

   @Override
   public void setOrdinal(int ordinal) {
      this.setBlock(BlockState.getFromOrdinal(ordinal));
   }

   @Override
   public BlockState getBlock() {
      return this.block.toBlockState();
   }

   @Override
   public void setBlock(BlockState state) {
      this.setFullBlock(state.toBaseBlock(this.block.getNbtData()));
   }

   @Override
   public BaseBlock getFullBlock() {
      return this.block;
   }

   @Override
   public void setFullBlock(BaseBlock block) {
      this.block = block;
   }

   @Override
   public void setBiome(BiomeType type) {
      this.biome = type;
   }

   @Override
   public BiomeType getBiome() {
      return this.biome;
   }

   @Override
   public CompoundTag getNbtData() {
      return this.block.getNbtData();
   }

   @Override
   public void setNbtData(@Nullable CompoundTag nbtData) {
      this.block = this.block.toBaseBlock(nbtData);
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return at(this.x(), this.y(), this.z());
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return at(this.x(), this.y(), this.z());
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      if (x == this.x() && y == this.y() && z == this.z()) {
         this.setFullBlock(block.toBaseBlock());
         return true;
      } else {
         return this.getExtent().setBlock(x, y, z, block);
      }
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return this.getExtent().setBiome(x, y, z, biome);
   }
}

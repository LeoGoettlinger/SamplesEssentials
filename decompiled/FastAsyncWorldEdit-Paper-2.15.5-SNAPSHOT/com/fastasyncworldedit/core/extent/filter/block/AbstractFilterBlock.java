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

public abstract class AbstractFilterBlock extends FilterBlock {
   @Override
   public abstract BaseBlock getFullBlock();

   @Override
   public abstract void setFullBlock(BaseBlock block);

   @Override
   public abstract BiomeType getBiome();

   @Override
   public abstract void setBiome(BiomeType type);

   public abstract BlockVector3 getPosition();

   @Override
   public abstract Extent getExtent();

   @Override
   public int x() {
      return this.getPosition().x();
   }

   @Override
   public int y() {
      return this.getPosition().y();
   }

   @Override
   public int z() {
      return this.getPosition().z();
   }

   @Override
   public int getOrdinal() {
      return this.getBlock().getOrdinal();
   }

   @Override
   public void setOrdinal(int ordinal) {
      this.setBlock(BlockState.getFromOrdinal(ordinal));
   }

   @Override
   public BlockState getBlock() {
      return this.getFullBlock().toBlockState();
   }

   @Override
   public CompoundTag getNbtData() {
      return this.getFullBlock().getNbtData();
   }

   @Override
   public void setNbtData(@Nullable CompoundTag nbtData) {
      this.setFullBlock(this.getFullBlock().toBaseBlock(nbtData));
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
   public void setBlock(BlockState state) {
      this.setFullBlock(state.toBaseBlock(this.getBlock().getNbtData()));
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
      if (x == this.x() && y == this.y() && z == this.z()) {
         this.setBiome(biome);
         return true;
      } else {
         return this.getExtent().setBiome(x, y, z, biome);
      }
   }
}

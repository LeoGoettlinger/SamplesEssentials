package com.fastasyncworldedit.core.extent.filter.block;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import javax.annotation.Nullable;

public abstract class FilterBlock extends BlockVector3 implements Extent, TileEntityBlock {
   public abstract Extent getExtent();

   public abstract int getOrdinal();

   public abstract void setOrdinal(int ordinal);

   public abstract BlockState getBlock();

   public abstract void setBlock(BlockState state);

   public abstract BaseBlock getFullBlock();

   public abstract void setFullBlock(BaseBlock block);

   public abstract void setBiome(BiomeType biome);

   public abstract BiomeType getBiome();

   @Override
   public BlockVector3 getMinimumPoint() {
      return this.getExtent().getMinimumPoint();
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.getExtent().getMaximumPoint();
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.getExtent().getBlock(x, y, z);
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tile) throws WorldEditException {
      return this.getExtent().tile(x, y, z, tile);
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      return this.getExtent().getFullBlock(x, y, z);
   }

   public BlockState getBlockBelow() {
      return this.getBlock(this.x(), this.y() - 1, this.z());
   }

   public BlockState getBlockAbove() {
      return this.getBlock(this.x(), this.y() + 1, this.z());
   }

   public BlockState getBlockNorth() {
      return this.getBlock(this.x(), this.y(), this.z() - 1);
   }

   public BlockState getBlockEast() {
      return this.getBlock(this.x() + 1, this.y(), this.z());
   }

   public BlockState getBlockSouth() {
      return this.getBlock(this.x(), this.y(), this.z() + 1);
   }

   public BlockState getBlockWest() {
      return this.getBlock(this.x() - 1, this.y(), this.z());
   }

   public BlockState getBlockRelativeY(int y) {
      return this.getBlock(this.x(), this.y() + y, this.z());
   }

   @Override
   public abstract int x();

   @Override
   public abstract int y();

   @Override
   public abstract int z();

   public int getLocalX() {
      return this.x() & 15;
   }

   public int getLocalY() {
      return this.y() & 15;
   }

   public int getLocalZ() {
      return this.z() & 15;
   }

   public int getChunkX() {
      return this.x() >> 4;
   }

   public int getChunkZ() {
      return this.z() >> 4;
   }

   @Override
   public boolean setOrdinal(Extent orDefault, int ordinal) {
      this.setOrdinal(ordinal);
      return true;
   }

   @Override
   public boolean setBlock(Extent orDefault, BlockState state) {
      this.setBlock(state);
      return true;
   }

   @Override
   public boolean setFullBlock(Extent orDefault, BaseBlock block) {
      this.setFullBlock(block);
      return true;
   }

   @Override
   public int getOrdinal(Extent orDefault) {
      return this.getOrdinal();
   }

   @Override
   public BlockState getBlock(Extent orDefault) {
      return this.getBlock();
   }

   @Override
   public BaseBlock getFullBlock(Extent orDefault) {
      return this.getFullBlock();
   }

   @Override
   public boolean setBiome(Extent orDefault, BiomeType type) {
      this.setBiome(type);
      return true;
   }

   @Override
   public BiomeType getBiome(Extent orDefault) {
      return this.getBiome();
   }

   @Override
   public CompoundTag getNbtData(Extent orDefault) {
      return this.getNbtData();
   }

   @Override
   public BlockState getOrdinalBelow(Extent orDefault) {
      return this.getBlockBelow();
   }

   @Override
   public BlockState getStateAbove(Extent orDefault) {
      return this.getBlockAbove();
   }

   @Override
   public BlockState getStateRelativeY(Extent orDefault, int y) {
      return this.getBlockRelativeY(y);
   }

   @Nullable
   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return null;
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.setBiome(position.x(), position.y(), position.z(), biome);
   }
}

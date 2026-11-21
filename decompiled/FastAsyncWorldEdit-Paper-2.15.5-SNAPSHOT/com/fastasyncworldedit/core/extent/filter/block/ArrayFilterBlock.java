package com.fastasyncworldedit.core.extent.filter.block;

import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypesCache;
import javax.annotation.Nullable;

@Deprecated(
   forRemoval = true,
   since = "2.3.0"
)
public class ArrayFilterBlock extends AbstractExtentFilterBlock {
   private final char[] blocks;
   private final byte[] heights;
   private final int yOffset;
   private final int width;
   private final int length;
   private int x;
   private int z;
   private int index;

   public ArrayFilterBlock(Extent extent, char[] blocks, byte[] heights, int width, int length, int yOffset) {
      super(extent);
      this.blocks = blocks;
      this.width = width;
      this.length = length;
      this.heights = heights;
      this.yOffset = yOffset;
   }

   public void init(int x, int z, int index) {
      this.x = x;
      this.z = z;
      this.index = index;
   }

   @Override
   public int getOrdinal() {
      return this.blocks[this.index];
   }

   @Override
   public void setOrdinal(int ordinal) {
      this.blocks[this.index] = (char)ordinal;
   }

   @Override
   public BlockState getBlock() {
      return BlockTypesCache.states[this.getOrdinal()];
   }

   @Override
   public BaseBlock getFullBlock() {
      return this.getBlock().toBaseBlock();
   }

   @Override
   public void setFullBlock(BaseBlock block) {
      this.blocks[this.index] = block.getOrdinalChar();
   }

   @Override
   public CompoundTag getNbtData() {
      return null;
   }

   @Override
   public void setNbtData(@Nullable CompoundTag nbtData) {
   }

   @Override
   public int x() {
      return this.x;
   }

   @Override
   public int y() {
      return (this.heights[this.index] & 0xFF) + this.yOffset;
   }

   @Override
   public int z() {
      return this.z;
   }

   @Override
   public void setBlock(BlockState state) {
      this.blocks[this.index] = state.getOrdinalChar();
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return this.getExtent().setBlock(x, y, z, block);
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tile) throws WorldEditException {
      return false;
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return this.getExtent().setBiome(x, y, z, biome);
   }

   @Override
   public void setBiome(final BiomeType biome) {
      this.getExtent().setBiome(this.x(), this.y(), this.z(), biome);
   }

   @Override
   public BiomeType getBiome() {
      return this.getExtent().getBiomeType(this.x(), this.y(), this.z());
   }
}

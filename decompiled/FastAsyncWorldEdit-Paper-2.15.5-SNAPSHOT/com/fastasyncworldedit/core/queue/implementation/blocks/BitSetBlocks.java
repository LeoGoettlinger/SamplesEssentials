package com.fastasyncworldedit.core.queue.implementation.blocks;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.collection.MemBlockSet;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;

public class BitSetBlocks implements IChunkSet {
   private final MemBlockSet.RowZ row;
   private final BlockState blockState;
   private final int minSectionPosition;
   private final int maxSectionPosition;
   private final int layers;
   private final int chunkX;
   private final int chunkZ;

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   public BitSetBlocks(BlockState blockState, int minSectionPosition, int maxSectionPosition) {
      this(blockState, minSectionPosition, maxSectionPosition, 0, 0);
   }

   public BitSetBlocks(BlockState blockState, int minSectionPosition, int maxSectionPosition, int chunkX, int chunkZ) {
      this.row = new MemBlockSet.RowZ(minSectionPosition, maxSectionPosition);
      this.blockState = blockState;
      this.minSectionPosition = minSectionPosition;
      this.maxSectionPosition = maxSectionPosition;
      this.layers = maxSectionPosition - minSectionPosition + 1;
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
   }

   @Override
   public boolean hasSection(int layer) {
      layer -= this.minSectionPosition;
      return this.row.rows[layer] != MemBlockSet.NULL_ROW_Y;
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return false;
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T holder) {
      y -= this.minSectionPosition << 4;
      this.row.set(null, x, y, z, this.minSectionPosition, this.maxSectionPosition);
      return true;
   }

   @Override
   public void setBlocks(int layer, char[] data) {
      layer -= this.minSectionPosition;
      this.row.reset(layer);
      int by = layer << 4;
      int y = 0;

      for (int index = 0; y < 16; y++) {
         for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; index++) {
               if (data[index] != 0) {
                  this.row.set(null, x, by + y, z, this.minSectionPosition, this.maxSectionPosition);
               }

               x++;
            }
         }
      }
   }

   @Override
   public boolean isEmpty() {
      return this.row.isEmpty();
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tag) {
      return false;
   }

   @Override
   public void setBlockLight(int x, int y, int z, int value) {
   }

   @Override
   public void setSkyLight(int x, int y, int z, int value) {
   }

   @Override
   public void setHeightMap(HeightMapType type, int[] heightMap) {
   }

   @Override
   public void setLightLayer(int layer, char[] toSet) {
   }

   @Override
   public void setSkyLightLayer(int layer, char[] toSet) {
   }

   @Override
   public void removeSectionLighting(int layer, boolean sky) {
   }

   @Override
   public void setFullBright(int layer) {
   }

   @Override
   public void entity(final FaweCompoundTag tag) {
   }

   @Override
   public void removeEntity(UUID uuid) {
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.row.get(null, x, y, z) ? this.blockState : null;
   }

   @Override
   public char[] load(int layer) {
      layer -= this.minSectionPosition;
      char[] arr = FaweCache.INSTANCE.SECTION_BITS_TO_CHAR.get();
      if (this.row.getRow(layer) instanceof MemBlockSet.RowY rowY) {
         char value = this.blockState.getOrdinalChar();
         long[] bits = rowY.getBits();
         int y = 0;
         int longIndex = 0;

         for (int blockIndex = 0; y < 16; y++) {
            for (int z = 0; z < 16; blockIndex += 64) {
               long bitBuffer = bits[longIndex];
               if (bitBuffer != 0L) {
                  if (bitBuffer == -1L) {
                     Arrays.fill(arr, blockIndex, blockIndex + 64, value);
                  } else {
                     Arrays.fill(arr, '\u0000');

                     do {
                        long lowBit = Long.lowestOneBit(bitBuffer);
                        int bitIndex = Long.bitCount(lowBit - 1L);
                        arr[blockIndex + bitIndex] = value;
                        bitBuffer ^= lowBit;
                     } while (bitBuffer != 0L);
                  }
               }

               z += 4;
               longIndex++;
            }
         }
      }

      return arr;
   }

   @Nullable
   @Override
   public char[] loadIfPresent(final int layer) {
      return this.load(layer);
   }

   @Override
   public BiomeType[][] getBiomes() {
      return null;
   }

   @Override
   public char[][] getLight() {
      return new char[0][];
   }

   @Override
   public char[][] getSkyLight() {
      return new char[0][];
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return null;
   }

   @Override
   public Map<BlockVector3, FaweCompoundTag> tiles() {
      return Collections.emptyMap();
   }

   @Nullable
   @Override
   public FaweCompoundTag tile(final int x, final int y, final int z) {
      return null;
   }

   @Override
   public Collection<FaweCompoundTag> entities() {
      return Collections.emptyList();
   }

   @Override
   public Set<UUID> getEntityRemoves() {
      return null;
   }

   @Override
   public IChunkSet reset() {
      this.row.reset();
      return this;
   }

   @Override
   public boolean hasBiomes(final int layer) {
      return false;
   }

   @Override
   public void setSideEffectSet(SideEffectSet sideEffectSet) {
   }

   @Override
   public SideEffectSet getSideEffectSet() {
      return SideEffectSet.none();
   }

   @Override
   public int getSectionCount() {
      return this.layers;
   }

   @Override
   public int getMaxSectionPosition() {
      return this.minSectionPosition;
   }

   @Override
   public int getMinSectionPosition() {
      return this.maxSectionPosition;
   }

   @Override
   public int getX() {
      return this.chunkX;
   }

   @Override
   public int getZ() {
      return this.chunkZ;
   }

   @Override
   public boolean trim(boolean aggressive) {
      return false;
   }

   @Override
   public boolean trim(boolean aggressive, int layer) {
      return false;
   }
}

package com.viaversion.viaversion.api.minecraft.chunks;

import com.viaversion.nbt.tag.CompoundTag;
import com.viaversion.viaversion.api.minecraft.blockentity.BlockEntity;
import java.util.BitSet;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Chunk1_21_5 implements Chunk {
   protected final int x;
   protected final int z;
   protected ChunkSection[] sections;
   protected Heightmap[] heightmaps;
   protected final List<BlockEntity> blockEntities;

   public Chunk1_21_5(int x, int z, ChunkSection[] sections, Heightmap[] heightmaps, List<BlockEntity> blockEntities) {
      this.x = x;
      this.z = z;
      this.sections = sections;
      this.heightmaps = heightmaps;
      this.blockEntities = blockEntities;
   }

   @Override
   public boolean isBiomeData() {
      return false;
   }

   @Override
   public int getX() {
      return this.x;
   }

   @Override
   public int getZ() {
      return this.z;
   }

   @Override
   public boolean isFullChunk() {
      return true;
   }

   @Override
   public boolean isIgnoreOldLightData() {
      return false;
   }

   @Override
   public void setIgnoreOldLightData(boolean ignoreOldLightData) {
      throw new UnsupportedOperationException();
   }

   @Override
   public int getBitmask() {
      return -1;
   }

   @Override
   public void setBitmask(int bitmask) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   public BitSet getChunkMask() {
      return null;
   }

   @Override
   public void setChunkMask(BitSet chunkSectionMask) {
      throw new UnsupportedOperationException();
   }

   @Override
   public ChunkSection[] getSections() {
      return this.sections;
   }

   @Override
   public void setSections(ChunkSection[] sections) {
      this.sections = sections;
   }

   @Nullable
   @Override
   public int[] getBiomeData() {
      return null;
   }

   @Override
   public void setBiomeData(@Nullable int[] biomeData) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   @Override
   public CompoundTag getHeightMap() {
      throw new UnsupportedOperationException();
   }

   @Override
   public void setHeightMap(CompoundTag heightMap) {
      throw new UnsupportedOperationException();
   }

   @Override
   public Heightmap[] heightmaps() {
      return this.heightmaps;
   }

   @Override
   public void setHeightmaps(Heightmap[] heightmaps) {
      this.heightmaps = heightmaps;
   }

   @Override
   public List<CompoundTag> getBlockEntities() {
      throw new UnsupportedOperationException();
   }

   @Override
   public List<BlockEntity> blockEntities() {
      return this.blockEntities;
   }
}

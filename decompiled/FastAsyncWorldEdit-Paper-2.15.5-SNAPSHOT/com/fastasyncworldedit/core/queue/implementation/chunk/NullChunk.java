package com.fastasyncworldedit.core.queue.implementation.chunk;

import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class NullChunk implements IQueueChunk {
   private static final NullChunk instance = new NullChunk();

   public static NullChunk getInstance() {
      return instance;
   }

   @Override
   public int getX() {
      return 0;
   }

   @Override
   public int getZ() {
      return 0;
   }

   @Override
   public boolean isEmpty() {
      return true;
   }

   @Override
   public void setWrapper(final WrapperChunk parentWrapper) {
   }

   @Override
   public void invalidateWrapper() {
   }

   @Override
   public Future call() {
      return null;
   }

   @Override
   public void filterBlocks(@Nonnull Filter filter, @Nonnull ChunkFilterBlock block, @Nullable Region region, boolean full) {
   }

   @Override
   public boolean setBiome(int x, int y, int z, @Nonnull BiomeType biome) {
      return false;
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tag) {
      return false;
   }

   @Override
   public void removeEntity(@Nonnull UUID uuid) {
   }

   @Nullable
   @Override
   public Set<UUID> getEntityRemoves() {
      return null;
   }

   @Override
   public int getSkyLight(int x, int y, int z) {
      return 15;
   }

   @Nonnull
   @Override
   public char[][] getLight() {
      return new char[0][];
   }

   @Nonnull
   @Override
   public char[][] getSkyLight() {
      return new char[0][];
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

   @Nonnull
   @Override
   public int[] getHeightMap(@Nullable HeightMapType type) {
      return new int[256];
   }

   @Override
   public int getEmittedLight(int x, int y, int z) {
      return 15;
   }

   @Override
   public void setSkyLight(int x, int y, int z, int value) {
   }

   @Override
   public void setHeightMap(@Nullable HeightMapType type, @Nullable int[] heightMap) {
   }

   @Override
   public boolean fullySupports3DBiomes() {
      return false;
   }

   @Override
   public void setBlockLight(int x, int y, int z, int value) {
   }

   @Override
   public void setFullBright(int layer) {
   }

   @Override
   public void entity(final FaweCompoundTag tag) {
   }

   @Override
   public void removeSectionLighting(int layer, boolean sky) {
   }

   @Override
   public void setSkyLightLayer(int layer, @Nullable char[] toSet) {
   }

   @Override
   public void setLightLayer(int layer, @Nullable char[] toSet) {
   }

   @Nullable
   @Override
   public BiomeType[][] getBiomes() {
      return null;
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T holder) {
      return false;
   }

   @Override
   public void setBlocks(int layer, @Nonnull char[] data) {
   }

   @Nullable
   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return null;
   }

   @Override
   public boolean hasSection(int layer) {
      return false;
   }

   @Nonnull
   @Override
   public BlockState getBlock(int x, int y, int z) {
      return BlockTypes.__RESERVED__.getDefaultState();
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

   @Nonnull
   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      return BlockTypes.__RESERVED__.getDefaultState().toBaseBlock();
   }

   @Nullable
   @Override
   public char[] load(int layer) {
      return null;
   }

   @Nullable
   @Override
   public char[] loadIfPresent(final int layer) {
      return null;
   }

   @Override
   public int setCreateCopy(boolean createCopy) {
      return -1;
   }

   @Override
   public Set<Entity> getFullEntities() {
      return Collections.emptySet();
   }

   @Override
   public boolean isCreateCopy() {
      return false;
   }

   @Override
   public void setLightingToGet(char[][] lighting, int minSectionPosition, int maxSectionPosition) {
   }

   @Override
   public void setSkyLightingToGet(char[][] lighting, int minSectionPosition, int maxSectionPosition) {
   }

   @Override
   public void setHeightmapToGet(HeightMapType type, int[] data) {
   }

   @Override
   public int getMaxY() {
      return 0;
   }

   @Override
   public int getMinY() {
      return 0;
   }

   @Override
   public int getMaxSectionPosition() {
      return 0;
   }

   @Override
   public int getMinSectionPosition() {
      return 0;
   }

   @Nullable
   @Override
   public <T extends Future<T>> T call(IQueueExtent<? extends IChunk> owner, @Nullable IChunkSet set, @Nullable Runnable finalize) {
      return null;
   }

   @Nullable
   @Override
   public FaweCompoundTag entity(final UUID uuid) {
      return null;
   }

   @Override
   public boolean trim(boolean aggressive) {
      return true;
   }

   @Override
   public boolean trim(boolean aggressive, int layer) {
      return true;
   }

   @Override
   public int getSectionCount() {
      return 0;
   }

   private NullChunk() {
   }
}

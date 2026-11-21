package com.fastasyncworldedit.core.queue.implementation.chunk;

import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class WrapperChunk<T extends IChunk> implements IChunk {
   private final Supplier<T> supplier;
   private volatile T chunk;

   public WrapperChunk(T initialValue, Supplier<T> supplier) {
      this.chunk = initialValue;
      this.supplier = supplier;
   }

   private T getWrapped() {
      T c = this.chunk;
      if (c != null) {
         return c;
      } else {
         synchronized (this) {
            if (this.chunk == null) {
               this.chunk = this.supplier.get();
            }

            return this.chunk;
         }
      }
   }

   public T get() {
      return this.chunk;
   }

   boolean invalidate(ChunkHolder<?> expected) {
      T c = this.chunk;
      if (c != null && expected != c) {
         return false;
      } else {
         synchronized (this) {
            this.chunk = null;
            return true;
         }
      }
   }

   public void setWrapped(T chunk) {
      synchronized (this) {
         this.chunk = Objects.requireNonNull(chunk);
      }
   }

   @Override
   public <V extends IChunk> void init(final IQueueExtent<V> extent, final int x, final int z) {
      this.getWrapped().init(extent, x, z);
   }

   @Override
   public int getX() {
      return this.getWrapped().getX();
   }

   @Override
   public int getZ() {
      return this.getWrapped().getZ();
   }

   @Override
   public byte[] toByteArray(final boolean full, final boolean stretched) {
      return this.getWrapped().toByteArray(full, stretched);
   }

   @Override
   public byte[] toByteArray(final byte[] buffer, final int bitMask, final boolean full, final boolean stretched) {
      return this.getWrapped().toByteArray(buffer, bitMask, full, stretched);
   }

   @Override
   public BlockVector3 getChunkBlockCoord() {
      return this.getWrapped().getChunkBlockCoord();
   }

   @Override
   public IChunk getRoot() {
      return this.getWrapped().getRoot();
   }

   @Override
   public void filterBlocks(final Filter filter, final ChunkFilterBlock block, @Nullable final Region region, final boolean full) {
      this.getWrapped().filterBlocks(filter, block, region, full);
   }

   @Override
   public boolean setBiome(final int x, final int y, final int z, final BiomeType biome) {
      return this.getWrapped().setBiome(x, y, z, biome);
   }

   @Override
   public boolean setBiome(final BlockVector3 position, final BiomeType biome) {
      return this.getWrapped().setBiome(position, biome);
   }

   @Override
   public void setBlockLight(final BlockVector3 position, final int value) {
      this.getWrapped().setBlockLight(position, value);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(final BlockVector3 position, final T block) throws WorldEditException {
      return this.getWrapped().setBlock(position, block);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(final int x, final int y, final int z, final T holder) {
      return this.getWrapped().setBlock(x, y, z, holder);
   }

   @Override
   public void setBlocks(final int layer, final char[] data) {
      this.getWrapped().setBlocks(layer, data);
   }

   @Override
   public boolean isEmpty() {
      return this.getWrapped().isEmpty();
   }

   @Override
   public boolean setTile(final int x, final int y, final int z, final CompoundTag tile) throws WorldEditException {
      return this.getWrapped().setTile(x, y, z, tile);
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tag) {
      return this.getWrapped().tile(x, y, z, tag);
   }

   @Override
   public boolean fullySupports3DBiomes() {
      return this.getWrapped().fullySupports3DBiomes();
   }

   @Override
   public boolean setBiome(final BlockVector2 position, final BiomeType biome) {
      return this.getWrapped().setBiome(position, biome);
   }

   @Override
   public void setBlockLight(final int x, final int y, final int z, final int value) {
      this.getWrapped().setBlockLight(x, y, z, value);
   }

   @Override
   public void setSkyLight(final BlockVector3 position, final int value) {
      this.getWrapped().setSkyLight(position, value);
   }

   @Override
   public void setSkyLight(final int x, final int y, final int z, final int value) {
      this.getWrapped().setSkyLight(x, y, z, value);
   }

   @Override
   public void setHeightMap(final HeightMapType type, final int[] heightMap) {
      this.getWrapped().setHeightMap(type, heightMap);
   }

   @Override
   public void setLightLayer(final int layer, final char[] toSet) {
      this.getWrapped().setLightLayer(layer, toSet);
   }

   @Override
   public void setSkyLightLayer(final int layer, final char[] toSet) {
      this.getWrapped().setSkyLightLayer(layer, toSet);
   }

   @Override
   public void setFullBright(final int layer) {
      this.getWrapped().setFullBright(layer);
   }

   @Override
   public void setEntity(final CompoundTag tag) {
      this.getWrapped().setEntity(tag);
   }

   @Override
   public void entity(final FaweCompoundTag tag) {
      this.getWrapped().entity(tag);
   }

   @Override
   public void removeEntity(final UUID uuid) {
      this.getWrapped().removeEntity(uuid);
   }

   @Override
   public Set<UUID> getEntityRemoves() {
      return this.getWrapped().getEntityRemoves();
   }

   @Override
   public BiomeType[][] getBiomes() {
      return this.getWrapped().getBiomes();
   }

   @Override
   public boolean hasBiomes() {
      return this.getWrapped().hasBiomes();
   }

   @Override
   public char[][] getLight() {
      return this.getWrapped().getLight();
   }

   @Override
   public char[][] getSkyLight() {
      return this.getWrapped().getSkyLight();
   }

   @Override
   public boolean hasLight() {
      return this.getWrapped().hasLight();
   }

   @Override
   public void setFastMode(final boolean fastMode) {
      this.getWrapped().setFastMode(fastMode);
   }

   @Override
   public boolean isFastMode() {
      return this.getWrapped().isFastMode();
   }

   @Override
   public void setBitMask(final int bitMask) {
      this.getWrapped().setBitMask(bitMask);
   }

   @Override
   public int getBitMask() {
      return this.getWrapped().getBitMask();
   }

   @Override
   public void removeSectionLighting(final int layer, final boolean sky) {
      this.getWrapped().removeSectionLighting(layer, sky);
   }

   @Override
   public boolean trim(final boolean aggressive, final int layer) {
      return this.getWrapped().trim(aggressive, layer);
   }

   @Override
   public Map<HeightMapType, int[]> getHeightMaps() {
      return this.getWrapped().getHeightMaps();
   }

   @Override
   public IChunk reset() {
      return this.getWrapped().reset();
   }

   @Override
   public int getSectionCount() {
      return this.getWrapped().getSectionCount();
   }

   @Override
   public int getMaxSectionPosition() {
      return this.getWrapped().getMaxSectionPosition();
   }

   @Override
   public int getMinSectionPosition() {
      return this.getWrapped().getMinSectionPosition();
   }

   @Nullable
   @Override
   public Operation commit() {
      return this.getWrapped().commit();
   }

   @Override
   public boolean hasBiomes(final int layer) {
      return this.getWrapped().hasBiomes(layer);
   }

   @NotNull
   @Override
   public IChunkSet createCopy() {
      return this.getWrapped().createCopy();
   }

   @Override
   public void setSideEffectSet(@NotNull final SideEffectSet sideEffectSet) {
      this.getWrapped().setSideEffectSet(sideEffectSet);
   }

   @NotNull
   @Override
   public SideEffectSet getSideEffectSet() {
      return this.getWrapped().getSideEffectSet();
   }

   @Override
   public BaseBlock getFullBlock(final int x, final int y, final int z) {
      return this.getWrapped().getFullBlock(x, y, z);
   }

   @Override
   public BiomeType getBiome(final BlockVector2 position) {
      return this.getWrapped().getBiome(position);
   }

   @Override
   public BiomeType getBiomeType(final int x, final int y, final int z) {
      return this.getWrapped().getBiomeType(x, y, z);
   }

   @Override
   public BiomeType getBiome(final BlockVector3 position) {
      return this.getWrapped().getBiome(position);
   }

   @Override
   public int getEmittedLight(final BlockVector3 position) {
      return this.getWrapped().getEmittedLight(position);
   }

   @Override
   public boolean hasSection(final int layer) {
      return this.getWrapped().hasSection(layer);
   }

   @Override
   public boolean hasNonEmptySection(final int layer) {
      return this.getWrapped().hasNonEmptySection(layer);
   }

   @Override
   public char[] load(final int layer) {
      return this.getWrapped().load(layer);
   }

   @Nullable
   @Override
   public char[] loadIfPresent(final int layer) {
      return this.getWrapped().loadIfPresent(layer);
   }

   @Override
   public BlockState getBlock(final BlockVector3 position) {
      return this.getWrapped().getBlock(position);
   }

   @Override
   public BlockState getBlock(final int x, final int y, final int z) {
      return this.getWrapped().getBlock(x, y, z);
   }

   @Override
   public BaseBlock getFullBlock(final BlockVector3 position) {
      return this.getWrapped().getFullBlock(position);
   }

   @Override
   public Map<BlockVector3, CompoundTag> getTiles() {
      return this.getWrapped().getTiles();
   }

   @Override
   public Map<BlockVector3, FaweCompoundTag> tiles() {
      return this.getWrapped().tiles();
   }

   @Override
   public int getSkyLight(final int x, final int y, final int z) {
      return this.getWrapped().getSkyLight(x, y, z);
   }

   @Override
   public int getBrightness(final MutableBlockVector3 position) {
      return this.getWrapped().getBrightness(position);
   }

   @Override
   public int getBrightness(final int x, final int y, final int z) {
      return this.getWrapped().getBrightness(x, y, z);
   }

   @Override
   public int getOpacity(final MutableBlockVector3 position) {
      return this.getWrapped().getOpacity(position);
   }

   @Override
   public int getOpacity(final int x, final int y, final int z) {
      return this.getWrapped().getOpacity(x, y, z);
   }

   @Override
   public int getEmittedLight(final int x, final int y, final int z) {
      return this.getWrapped().getEmittedLight(x, y, z);
   }

   @Override
   public int getSkyLight(final MutableBlockVector3 position) {
      return this.getWrapped().getSkyLight(position);
   }

   @Override
   public int[] getHeightMap(final HeightMapType type) {
      return this.getWrapped().getHeightMap(type);
   }

   @Override
   public void optimize() {
      this.getWrapped().optimize();
   }

   @Override
   public <T extends Future<T>> T call(final IQueueExtent<?> owner, final IChunkSet set, final Runnable finalize) {
      return this.getWrapped().call((IQueueExtent<? extends IChunk>)owner, set, finalize);
   }

   @Override
   public CompoundTag getEntity(final UUID uuid) {
      return this.getWrapped().getEntity(uuid);
   }

   @Nullable
   @Override
   public FaweCompoundTag entity(final UUID uuid) {
      return this.getWrapped().entity(uuid);
   }

   @Override
   public CompoundTag getTile(final int x, final int y, final int z) {
      return this.getWrapped().getTile(x, y, z);
   }

   @Nullable
   @Override
   public FaweCompoundTag tile(final int x, final int y, final int z) {
      return this.getWrapped().tile(x, y, z);
   }

   @Override
   public Set<CompoundTag> getEntities() {
      return this.getWrapped().getEntities();
   }

   @Override
   public Collection<FaweCompoundTag> entities() {
      return this.getWrapped().entities();
   }

   @Override
   public Set<Entity> getFullEntities() {
      return this.getWrapped().getFullEntities();
   }

   @Override
   public boolean isCreateCopy() {
      return this.getWrapped().isCreateCopy();
   }

   @Override
   public int setCreateCopy(final boolean createCopy) {
      return this.getWrapped().setCreateCopy(createCopy);
   }

   @Nullable
   @Override
   public IChunkGet getCopy(final int key) {
      return this.getWrapped().getCopy(key);
   }

   @Override
   public void lockCall() {
      this.getWrapped().lockCall();
   }

   @Override
   public void unlockCall() {
      this.getWrapped().unlockCall();
   }

   @Override
   public void setLightingToGet(final char[][] lighting, final int startSectionIndex, final int endSectionIndex) {
      this.getWrapped().setLightingToGet(lighting, startSectionIndex, endSectionIndex);
   }

   @Override
   public void setSkyLightingToGet(final char[][] lighting, final int startSectionIndex, final int endSectionIndex) {
      this.getWrapped().setSkyLightingToGet(lighting, startSectionIndex, endSectionIndex);
   }

   @Override
   public void setHeightmapToGet(final HeightMapType type, final int[] data) {
      this.getWrapped().setHeightmapToGet(type, data);
   }

   @Override
   public int getMaxY() {
      return this.getWrapped().getMaxY();
   }

   @Override
   public int getMinY() {
      return this.getWrapped().getMinY();
   }

   @Override
   public boolean trim(final boolean aggressive) {
      return this.getWrapped().trim(aggressive);
   }

   @Override
   public void recycle() {
      this.getWrapped().recycle();
   }
}

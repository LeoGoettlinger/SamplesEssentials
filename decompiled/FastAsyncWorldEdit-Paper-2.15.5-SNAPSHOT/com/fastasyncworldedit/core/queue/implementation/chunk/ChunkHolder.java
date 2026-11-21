package com.fastasyncworldedit.core.queue.implementation.chunk;

import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.extent.processor.EmptyBatchProcessor;
import com.fastasyncworldedit.core.extent.processor.heightmap.HeightMapType;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.util.MemUtil;
import com.fastasyncworldedit.core.util.task.FaweThreadUtil;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class ChunkHolder<T extends Future<T>> implements IQueueChunk<T> {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private volatile IChunkGet chunkExisting;
   private volatile IChunkSet chunkSet;
   private ChunkHolder.IBlockDelegate delegate;
   private IQueueExtent<?> extent;
   private int chunkX;
   private int chunkZ;
   private boolean fastmode;
   private int bitMask = -1;
   private boolean isInit = false;
   private boolean createCopy = false;
   private long initTime = -1L;
   private SideEffectSet sideEffectSet;
   private WrapperChunk<?> parentWrapper = null;
   private static final AtomicBoolean recycleWarning = new AtomicBoolean(false);
   private static final ChunkHolder.IBlockDelegate BOTH = new ChunkHolder.IBlockDelegate() {
      @Override
      public IChunkGet get(ChunkHolder chunk) {
         return chunk.chunkExisting;
      }

      @Override
      public IChunkSet set(ChunkHolder chunk) {
         return chunk.chunkSet;
      }

      @Override
      public boolean setBiome(ChunkHolder chunk, int x, int y, int z, BiomeType biome) {
         return chunk.chunkSet.setBiome(x, y, z, biome);
      }

      @Override
      public <B extends BlockStateHolder<B>> boolean setBlock(ChunkHolder chunk, int x, int y, int z, B block) {
         return chunk.chunkSet.setBlock(x, y, z, block);
      }

      @Override
      public void setSkyLight(ChunkHolder chunk, int x, int y, int z, int value) {
         chunk.chunkSet.setSkyLight(x, y, z, value);
      }

      @Override
      public void setBlockLight(ChunkHolder chunk, int x, int y, int z, int value) {
         chunk.chunkSet.setBlockLight(x, y, z, value);
      }

      @Override
      public void removeSectionLighting(ChunkHolder chunk, int layer, boolean sky) {
         chunk.chunkSet.removeSectionLighting(layer, sky);
         chunk.chunkExisting.removeSectionLighting(layer, sky);
      }

      @Override
      public void setFullBright(ChunkHolder chunk, int layer) {
         chunk.chunkSet.setFullBright(layer);
      }

      @Override
      public void setLightLayer(ChunkHolder chunk, int layer, char[] toSet) {
         chunk.chunkSet.setLightLayer(layer, toSet);
      }

      @Override
      public void setSkyLightLayer(ChunkHolder chunk, int layer, char[] toSet) {
         chunk.chunkSet.setSkyLightLayer(layer, toSet);
      }

      @Override
      public void setHeightMap(ChunkHolder chunk, HeightMapType type, int[] heightMap) {
         chunk.chunkSet.setHeightMap(type, heightMap);
      }

      @Override
      public BiomeType getBiome(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getBiomeType(x, y, z);
      }

      @Override
      public BlockState getBlock(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getBlock(x, y, z);
      }

      @Override
      public BaseBlock getFullBlock(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getFullBlock(x, y, z);
      }

      @Override
      public int getSkyLight(ChunkHolder chunk, int x, int y, int z) {
         if (chunk.chunkSet.getSkyLight() != null) {
            int layer = y >> 4;
            layer -= chunk.chunkSet.getMinSectionPosition();
            if (layer >= 0 && layer < chunk.chunkSet.getSectionCount() && chunk.chunkSet.getSkyLight()[layer] != null) {
               int setLightValue = chunk.chunkSet.getSkyLight()[layer][(y & 15) << 8 | (z & 15) << 4 | x & 15];
               if (setLightValue < 16) {
                  return setLightValue;
               }
            }
         }

         return chunk.chunkExisting.getSkyLight(x, y, z);
      }

      @Override
      public int getEmittedLight(ChunkHolder chunk, int x, int y, int z) {
         if (chunk.chunkSet.getLight() != null) {
            int layer = y >> 4;
            layer -= chunk.chunkSet.getMinSectionPosition();
            if (layer >= 0 && layer < chunk.chunkSet.getSectionCount() && chunk.chunkSet.getLight()[layer] != null) {
               int setLightValue = chunk.chunkSet.getLight()[layer][(y & 15) << 8 | (z & 15) << 4 | x & 15];
               if (setLightValue < 16) {
                  return setLightValue;
               }
            }
         }

         return chunk.chunkExisting.getEmittedLight(x, y, z);
      }

      @Override
      public int getBrightness(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getBrightness(x, y, z);
      }

      @Override
      public int getOpacity(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getOpacity(x, y, z);
      }

      @Override
      public int[] getHeightMap(ChunkHolder chunk, HeightMapType type) {
         return chunk.chunkExisting.getHeightMap(type);
      }

      @Override
      public void flushLightToGet(ChunkHolder chunk) {
         chunk.chunkExisting.setLightingToGet(chunk.chunkSet.getLight(), chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
         chunk.chunkExisting.setSkyLightingToGet(chunk.chunkSet.getSkyLight(), chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
      }

      @Override
      public void setLightingToGet(ChunkHolder chunk, char[][] lighting) {
         chunk.chunkExisting.setLightingToGet(lighting, chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
      }

      @Override
      public void setSkyLightingToGet(ChunkHolder chunk, char[][] lighting) {
         chunk.chunkExisting.setSkyLightingToGet(lighting, chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
      }

      @Override
      public void setHeightmapToGet(ChunkHolder chunk, HeightMapType type, int[] data) {
         chunk.chunkExisting.setHeightmapToGet(type, data);
      }
   };
   private static final ChunkHolder.IBlockDelegate GET = new ChunkHolder.IBlockDelegate() {
      @Override
      public IChunkGet get(ChunkHolder chunk) {
         return chunk.chunkExisting;
      }

      @Override
      public IChunkSet set(ChunkHolder chunk) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.chunkSet;
      }

      @Override
      public boolean setBiome(ChunkHolder chunk, int x, int y, int z, BiomeType biome) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.setBiome(x, y, z, biome);
      }

      @Override
      public <B extends BlockStateHolder<B>> boolean setBlock(ChunkHolder chunk, int x, int y, int z, B block) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.setBlock(x, y, z, block);
      }

      @Override
      public void setSkyLight(ChunkHolder chunk, int x, int y, int z, int value) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.setSkyLight(x, y, z, value);
      }

      @Override
      public void setBlockLight(ChunkHolder chunk, int x, int y, int z, int value) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.setBlockLight(x, y, z, value);
      }

      @Override
      public void removeSectionLighting(ChunkHolder chunk, int layer, boolean sky) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.removeSectionLighting(layer, sky);
      }

      @Override
      public void setFullBright(ChunkHolder chunk, int layer) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.setFullBright(layer);
      }

      @Override
      public void setLightLayer(ChunkHolder chunk, int layer, char[] toSet) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.setLightLayer(layer, toSet);
      }

      @Override
      public void setSkyLightLayer(ChunkHolder chunk, int layer, char[] toSet) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.setSkyLightLayer(layer, toSet);
      }

      @Override
      public void setHeightMap(ChunkHolder chunk, HeightMapType type, int[] heightMap) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.setHeightMap(type, heightMap);
      }

      @Override
      public BiomeType getBiome(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getBiomeType(x, y, z);
      }

      @Override
      public BlockState getBlock(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getBlock(x, y, z);
      }

      @Override
      public BaseBlock getFullBlock(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getFullBlock(x, y, z);
      }

      @Override
      public int getSkyLight(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getSkyLight(x, y, z);
      }

      @Override
      public int getEmittedLight(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getEmittedLight(x, y, z);
      }

      @Override
      public int getBrightness(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getBrightness(x, y, z);
      }

      @Override
      public int getOpacity(ChunkHolder chunk, int x, int y, int z) {
         return chunk.chunkExisting.getOpacity(x, y, z);
      }

      @Override
      public int[] getHeightMap(ChunkHolder chunk, HeightMapType type) {
         return chunk.chunkExisting.getHeightMap(type);
      }

      @Override
      public void flushLightToGet(ChunkHolder chunk) {
      }

      @Override
      public void setLightingToGet(ChunkHolder chunk, char[][] lighting) {
         chunk.chunkExisting.setLightingToGet(lighting, chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
      }

      @Override
      public void setSkyLightingToGet(ChunkHolder chunk, char[][] lighting) {
         chunk.chunkExisting.setSkyLightingToGet(lighting, chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
      }

      @Override
      public void setHeightmapToGet(ChunkHolder chunk, HeightMapType type, int[] data) {
         chunk.chunkExisting.setHeightmapToGet(type, data);
      }
   };
   private static final ChunkHolder.IBlockDelegate SET = new ChunkHolder.IBlockDelegate() {
      @Override
      public IChunkGet get(ChunkHolder chunk) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.chunkExisting;
      }

      @Override
      public IChunkSet set(ChunkHolder chunk) {
         return chunk.chunkSet;
      }

      @Override
      public boolean setBiome(ChunkHolder chunk, int x, int y, int z, BiomeType biome) {
         return chunk.chunkSet.setBiome(x, y, z, biome);
      }

      @Override
      public <B extends BlockStateHolder<B>> boolean setBlock(ChunkHolder chunk, int x, int y, int z, B block) {
         return chunk.chunkSet.setBlock(x, y, z, block);
      }

      @Override
      public void setSkyLight(ChunkHolder chunk, int x, int y, int z, int value) {
         chunk.chunkSet.setSkyLight(x, y, z, value);
      }

      @Override
      public void setBlockLight(ChunkHolder chunk, int x, int y, int z, int value) {
         chunk.chunkSet.setBlockLight(x, y, z, value);
      }

      @Override
      public void removeSectionLighting(ChunkHolder chunk, int layer, boolean sky) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.removeSectionLighting(layer, sky);
      }

      @Override
      public void setFullBright(ChunkHolder chunk, int layer) {
         chunk.chunkSet.setFullBright(layer);
      }

      @Override
      public void setLightLayer(ChunkHolder chunk, int layer, char[] toSet) {
         chunk.chunkSet.setLightLayer(layer, toSet);
      }

      @Override
      public void setSkyLightLayer(ChunkHolder chunk, int layer, char[] toSet) {
         chunk.chunkSet.setSkyLightLayer(layer, toSet);
      }

      @Override
      public void setHeightMap(ChunkHolder chunk, HeightMapType type, int[] heightMap) {
         chunk.chunkSet.setHeightMap(type, heightMap);
      }

      @Override
      public BiomeType getBiome(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.getBiomeType(x, y, z);
      }

      @Override
      public BlockState getBlock(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.getBlock(x, y, z);
      }

      @Override
      public BaseBlock getFullBlock(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.getFullBlock(x, y, z);
      }

      @Override
      public int getSkyLight(ChunkHolder chunk, int x, int y, int z) {
         if (chunk.chunkSet.getSkyLight() != null) {
            int layer = y >> 4;
            layer -= chunk.chunkSet.getMinSectionPosition();
            if (layer >= 0 && layer < chunk.chunkSet.getSectionCount() && chunk.chunkSet.getSkyLight()[layer] != null) {
               int setLightValue = chunk.chunkSet.getSkyLight()[layer][(y & 15) << 8 | (z & 15) << 4 | x & 15];
               if (setLightValue < 16) {
                  return setLightValue;
               }
            }
         }

         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.getSkyLight(x, y, z);
      }

      @Override
      public int getEmittedLight(ChunkHolder chunk, int x, int y, int z) {
         if (chunk.chunkSet.getLight() != null) {
            int layer = y >> 4;
            layer -= chunk.chunkSet.getMinSectionPosition();
            if (layer >= 0 && layer < chunk.chunkSet.getSectionCount() && chunk.chunkSet.getLight()[layer] != null) {
               int setLightValue = chunk.chunkSet.getLight()[layer][(y & 15) << 8 | (z & 15) << 4 | x & 15];
               if (setLightValue < 16) {
                  return setLightValue;
               }
            }
         }

         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.getEmittedLight(x, y, z);
      }

      @Override
      public int getBrightness(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.getBrightness(x, y, z);
      }

      @Override
      public int getOpacity(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.getOpacity(x, y, z);
      }

      @Override
      public int[] getHeightMap(ChunkHolder chunk, HeightMapType type) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         return chunk.getHeightMap(type);
      }

      @Override
      public void flushLightToGet(ChunkHolder chunk) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.flushLightToGet();
      }

      @Override
      public void setLightingToGet(ChunkHolder chunk, char[][] lighting) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.setLightingToGet(lighting, chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
      }

      @Override
      public void setSkyLightingToGet(ChunkHolder chunk, char[][] lighting) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.setSkyLightingToGet(lighting, chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
      }

      @Override
      public void setHeightmapToGet(ChunkHolder chunk, HeightMapType type, int[] data) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.setHeightmapToGet(type, data);
      }
   };
   private static final ChunkHolder.IBlockDelegate NULL = new ChunkHolder.IBlockDelegate() {
      @Override
      public IChunkGet get(ChunkHolder chunk) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         return chunk.chunkExisting;
      }

      @Override
      public IChunkSet set(ChunkHolder chunk) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.SET;
         return chunk.chunkSet;
      }

      @Override
      public boolean setBiome(ChunkHolder chunk, int x, int y, int z, BiomeType biome) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.SET;
         return chunk.setBiome(x, y, z, biome);
      }

      @Override
      public <B extends BlockStateHolder<B>> boolean setBlock(ChunkHolder chunk, int x, int y, int z, B block) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.SET;
         return chunk.setBlock(x, y, z, block);
      }

      @Override
      public BiomeType getBiome(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         return chunk.getBiomeType(x, y, z);
      }

      @Override
      public BlockState getBlock(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         return chunk.getBlock(x, y, z);
      }

      @Override
      public BaseBlock getFullBlock(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         return chunk.getFullBlock(x, y, z);
      }

      @Override
      public void setSkyLight(ChunkHolder chunk, int x, int y, int z, int value) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.SET;
         chunk.setSkyLight(x, y, z, value);
      }

      @Override
      public void setBlockLight(ChunkHolder chunk, int x, int y, int z, int value) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.SET;
         chunk.setBlockLight(x, y, z, value);
      }

      @Override
      public void removeSectionLighting(ChunkHolder chunk, int layer, boolean sky) {
         chunk.getOrCreateGet();
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.BOTH;
         chunk.removeSectionLighting(layer, sky);
      }

      @Override
      public void setFullBright(ChunkHolder chunk, int layer) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.SET;
         chunk.setFullBright(layer);
      }

      @Override
      public void setLightLayer(ChunkHolder chunk, int layer, char[] toSet) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.SET;
         chunk.setLightLayer(layer, toSet);
      }

      @Override
      public void setSkyLightLayer(ChunkHolder chunk, int layer, char[] toSet) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.SET;
         chunk.setSkyLightLayer(layer, toSet);
      }

      @Override
      public void setHeightMap(ChunkHolder chunk, HeightMapType type, int[] heightMap) {
         chunk.getOrCreateSet();
         chunk.delegate = ChunkHolder.SET;
         chunk.setHeightMap(type, heightMap);
      }

      @Override
      public int getSkyLight(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         return chunk.getSkyLight(x, y, z);
      }

      @Override
      public int getEmittedLight(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         return chunk.getEmittedLight(x, y, z);
      }

      @Override
      public int getBrightness(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         return chunk.getBrightness(x, y, z);
      }

      @Override
      public int getOpacity(ChunkHolder chunk, int x, int y, int z) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         return chunk.getOpacity(x, y, z);
      }

      @Override
      public int[] getHeightMap(ChunkHolder chunk, HeightMapType type) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         return chunk.getHeightMap(type);
      }

      @Override
      public void flushLightToGet(ChunkHolder chunk) {
      }

      @Override
      public void setLightingToGet(ChunkHolder chunk, char[][] lighting) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         chunk.setLightingToGet(lighting, chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
      }

      @Override
      public void setSkyLightingToGet(ChunkHolder chunk, char[][] lighting) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         chunk.setSkyLightingToGet(lighting, chunk.chunkSet.getMinSectionPosition(), chunk.chunkSet.getMaxSectionPosition());
      }

      @Override
      public void setHeightmapToGet(ChunkHolder chunk, HeightMapType type, int[] data) {
         chunk.getOrCreateGet();
         chunk.delegate = ChunkHolder.GET;
         chunk.setHeightmapToGet(type, data);
      }
   };

   public static ChunkHolder newInstance() {
      return new ChunkHolder();
   }

   private ChunkHolder() {
      this.delegate = NULL;
   }

   public void init(ChunkHolder.IBlockDelegate delegate) {
      this.delegate = delegate;
   }

   @Override
   public void setWrapper(WrapperChunk<?> parentWrapper) {
      if (parentWrapper != this.parentWrapper) {
         if (this.parentWrapper != null) {
            throw new IllegalStateException("Wrapper already set");
         } else {
            this.parentWrapper = parentWrapper;
         }
      }
   }

   @Override
   public void invalidateWrapper() {
      if (this.parentWrapper != null && !this.parentWrapper.invalidate(this)) {
         throw new IllegalStateException("Existing chunk not equal to expected");
      }
   }

   @Override
   public void recycle() {
      if (!recycleWarning.getAndSet(true)) {
         LOGGER.warn("ChunkHolder should not be recycled.", new Exception());
      }
   }

   public long initAge() {
      return System.currentTimeMillis() - this.initTime;
   }

   public synchronized ChunkHolder.IBlockDelegate getDelegate() {
      return this.delegate;
   }

   @Override
   public void entity(final FaweCompoundTag tag) {
      this.delegate.set(this).entity(tag);
   }

   @Override
   public void removeEntity(UUID uuid) {
      this.delegate.set(this).removeEntity(uuid);
   }

   @Override
   public Set<UUID> getEntityRemoves() {
      return this.delegate.set(this).getEntityRemoves();
   }

   @Override
   public BiomeType[][] getBiomes() {
      return this.delegate.set(this).getBiomes();
   }

   @Override
   public char[][] getLight() {
      return this.delegate.set(this).getLight();
   }

   @Override
   public char[][] getSkyLight() {
      return this.delegate.set(this).getSkyLight();
   }

   @Override
   public void setBlocks(int layer, char[] data) {
      this.delegate.set(this).setBlocks(layer, data);
   }

   @Override
   public char[] load(int layer) {
      return this.getOrCreateGet().load(layer);
   }

   @Nullable
   @Override
   public char[] loadIfPresent(final int layer) {
      return this.chunkExisting == null ? null : this.chunkExisting.loadIfPresent(layer);
   }

   @Override
   public boolean isFastMode() {
      return this.fastmode;
   }

   @Override
   public void setFastMode(boolean fastmode) {
      this.fastmode = fastmode;
   }

   @Override
   public void setBitMask(int bitMask) {
      this.bitMask = bitMask;
   }

   @Override
   public int getBitMask() {
      return this.bitMask;
   }

   @Override
   public boolean hasBiomes(final int layer) {
      return this.chunkSet != null && this.chunkSet.hasBiomes(layer);
   }

   @Override
   public void setSideEffectSet(SideEffectSet sideEffectSet) {
      this.sideEffectSet = sideEffectSet;
   }

   @Override
   public SideEffectSet getSideEffectSet() {
      return this.sideEffectSet;
   }

   public boolean isInit() {
      return this.isInit;
   }

   @Override
   public int setCreateCopy(boolean createCopy) {
      this.createCopy = createCopy;
      return -1;
   }

   @Override
   public boolean isCreateCopy() {
      return this.createCopy;
   }

   @Override
   public void setLightingToGet(char[][] lighting, int minSectionPosition, int maxSectionPosition) {
      this.delegate.setLightingToGet(this, lighting);
   }

   @Override
   public void setSkyLightingToGet(char[][] lighting, int minSectionPosition, int maxSectionPosition) {
      this.delegate.setSkyLightingToGet(this, lighting);
   }

   @Override
   public void setHeightmapToGet(HeightMapType type, int[] data) {
      this.delegate.setHeightmapToGet(this, type, data);
   }

   @Override
   public int getMaxY() {
      return this.getOrCreateGet().getMaxY();
   }

   @Override
   public int getMinY() {
      return this.getOrCreateGet().getMinY();
   }

   @Override
   public int getMaxSectionPosition() {
      return this.getOrCreateGet().getMaxSectionPosition();
   }

   @Override
   public int getMinSectionPosition() {
      return this.getOrCreateGet().getMinSectionPosition();
   }

   public void flushLightToGet() {
      this.delegate.flushLightToGet(this);
   }

   @Override
   public boolean hasSection(int layer) {
      return this.chunkExisting != null && this.chunkExisting.hasSection(layer);
   }

   @Override
   public boolean hasNonEmptySection(final int layer) {
      return this.chunkExisting != null && this.chunkExisting.hasNonEmptySection(layer);
   }

   @Override
   public synchronized void filterBlocks(Filter filter, ChunkFilterBlock block, @Nullable Region region, boolean full) {
      IChunkGet get = this.getOrCreateGet();
      IChunkSet set = this.getOrCreateSet();
      if (this.parentWrapper == null) {
         this.parentWrapper = new WrapperChunk<>(this, () -> this.extent.getOrCreateChunk(this.getX(), this.getZ()));
      } else if (this.parentWrapper.get() != this) {
         throw new IllegalStateException("Parent WrapperChunk is not storing this chunk!?");
      }

      try {
         block.filter(this.parentWrapper, get, set, filter, region, full);
      } finally {
         filter.finishChunk(this.parentWrapper);
      }
   }

   @Override
   public synchronized boolean trim(boolean aggressive) {
      this.chunkExisting.trim(aggressive);
      if (this.chunkSet != null) {
         boolean result = this.chunkSet.trim(aggressive);
         if (result) {
            this.delegate = NULL;
            this.chunkExisting = null;
            this.chunkSet.recycle();
            this.chunkSet = null;
            return true;
         }
      }

      if (aggressive) {
         this.chunkExisting = null;
         if (this.delegate == BOTH) {
            this.delegate = SET;
         } else if (this.delegate == GET) {
            this.delegate = NULL;
         }
      }

      return false;
   }

   @Override
   public synchronized boolean trim(boolean aggressive, int layer) {
      this.chunkExisting.trim(aggressive, layer);
      return this.trim(aggressive);
   }

   @Override
   public int getSectionCount() {
      return this.getOrCreateGet().getSectionCount();
   }

   @Override
   public boolean isEmpty() {
      return this.chunkSet == null || this.chunkSet.isEmpty();
   }

   @Override
   public boolean tile(final int x, final int y, final int z, final FaweCompoundTag tag) {
      return this.delegate.set(this).tile(x, y, z, tag);
   }

   public final IChunkGet getOrCreateGet() {
      if (this.chunkExisting == null) {
         this.chunkExisting = this.newWrappedGet();
         this.chunkExisting.trim(MemUtil.isMemoryLimited());
      }

      return this.chunkExisting;
   }

   public final IChunkSet getOrCreateSet() {
      if (this.chunkSet == null) {
         this.chunkSet = this.newWrappedSet();
      }

      return this.chunkSet;
   }

   public final IChunkSet getChunkSet() {
      return this.chunkSet;
   }

   private IChunkSet newWrappedSet() {
      IChunkSet set = this.extent.getCachedSet(this.chunkX, this.chunkZ);
      set.setFastMode(this.fastmode);
      set.setSideEffectSet(this.sideEffectSet);
      set.setBitMask(this.bitMask);
      return set;
   }

   private IChunkGet newWrappedGet() {
      return this.extent.getCachedGet(this.chunkX, this.chunkZ);
   }

   @Override
   public synchronized <V extends IChunk> void init(IQueueExtent<V> extent, int chunkX, int chunkZ) {
      this.initTime = System.currentTimeMillis();
      this.extent = extent;
      this.chunkX = chunkX;
      this.chunkZ = chunkZ;
      if (this.parentWrapper != null) {
         if (!this.parentWrapper.invalidate(this)) {
            throw new IllegalStateException("Existing chunk not equal to expected");
         }

         this.parentWrapper = null;
      }

      if (this.chunkSet != null) {
         this.chunkSet.reset();
         this.delegate = SET;
      } else {
         this.delegate = NULL;
      }

      this.chunkExisting = null;
      this.isInit = true;
   }

   @Override
   public synchronized T call() {
      if (this.chunkSet != null && !this.chunkSet.isEmpty()) {
         IChunkSet copy = this.chunkSet.createCopy();
         return this.call(this.extent, copy, () -> {});
      } else {
         return null;
      }
   }

   @Override
   public <U extends Future<U>> U call(IQueueExtent<?> owner, IChunkSet set, Runnable finalize) {
      if (set != null) {
         if (this.parentWrapper != null && !this.parentWrapper.invalidate(this)) {
            throw new IllegalStateException("Existing chunk not equal to expected");
         } else {
            IChunkGet get = this.getOrCreateGet();

            Future var9;
            try {
               get.lockCall();
               this.trackExtent();
               boolean postProcess = !(this.getExtent().getPostProcessor() instanceof EmptyBatchProcessor);
               int copyKey = get.setCreateCopy(postProcess);
               IChunkSet iChunkSet = this.getExtent().processSet(this, get, set);
               Runnable finalizer;
               if (postProcess) {
                  finalizer = () -> {
                     this.getExtent().postProcess(this, get.getCopy(copyKey), iChunkSet);
                     finalize.run();
                  };
               } else {
                  finalizer = finalize;
               }

               var9 = get.call((IQueueExtent<? extends IChunk>)this.extent, set, finalizer);
            } finally {
               get.unlockCall();
               this.untrackExtent();
            }

            return (U)var9;
         }
      } else {
         return null;
      }
   }

   private void trackExtent() {
      FaweThreadUtil.setCurrentExtent(this.extent);
   }

   private void untrackExtent() {
      FaweThreadUtil.clearCurrentExtent();
   }

   public IQueueExtent<?> getExtent() {
      return this.extent;
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
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return this.delegate.setBiome(this, x, y, z, biome);
   }

   @Override
   public <B extends BlockStateHolder<B>> boolean setBlock(int x, int y, int z, B block) {
      return this.delegate.setBlock(this, x, y, z, block);
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return this.delegate.getBiome(this, x, y, z);
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.delegate.getBlock(this, x, y, z);
   }

   @Override
   public Map<BlockVector3, FaweCompoundTag> tiles() {
      return this.delegate.get(this).tiles();
   }

   @Nullable
   @Override
   public FaweCompoundTag tile(final int x, final int y, final int z) {
      return this.delegate.get(this).tile(x, y, z);
   }

   @Override
   public Collection<FaweCompoundTag> entities() {
      return this.delegate.get(this).entities();
   }

   @Override
   public Set<Entity> getFullEntities() {
      return this.delegate.get(this).getFullEntities();
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      return this.delegate.getFullBlock(this, x, y, z);
   }

   @Override
   public void setSkyLight(int x, int y, int z, int value) {
      this.delegate.setSkyLight(this, x, y, z, value);
   }

   @Override
   public void setHeightMap(HeightMapType type, int[] heightMap) {
      this.delegate.setHeightMap(this, type, heightMap);
   }

   @Override
   public void removeSectionLighting(int layer, boolean sky) {
      this.delegate.removeSectionLighting(this, layer, sky);
   }

   @Override
   public void setFullBright(int layer) {
      this.delegate.setFullBright(this, layer);
   }

   @Override
   public void setBlockLight(int x, int y, int z, int value) {
      this.delegate.setBlockLight(this, x, y, z, value);
   }

   @Override
   public void setLightLayer(int layer, char[] toSet) {
      this.delegate.setLightLayer(this, layer, toSet);
   }

   @Override
   public void setSkyLightLayer(int layer, char[] toSet) {
      this.delegate.setSkyLightLayer(this, layer, toSet);
   }

   @Override
   public int getSkyLight(int x, int y, int z) {
      return this.delegate.getSkyLight(this, x, y, z);
   }

   @Override
   public int getEmittedLight(int x, int y, int z) {
      return this.delegate.getEmittedLight(this, x, y, z);
   }

   @Override
   public int getBrightness(int x, int y, int z) {
      return this.delegate.getBrightness(this, x, y, z);
   }

   @Override
   public int getOpacity(int x, int y, int z) {
      return this.delegate.getOpacity(this, x, y, z);
   }

   @Override
   public int[] getHeightMap(HeightMapType type) {
      return this.delegate.getHeightMap(this, type);
   }

   @Nullable
   @Override
   public FaweCompoundTag entity(final UUID uuid) {
      return this.delegate.get(this).entity(uuid);
   }

   public interface IBlockDelegate {
      <C extends Future<C>> IChunkGet get(ChunkHolder<C> chunk);

      IChunkSet set(ChunkHolder chunk);

      boolean setBiome(ChunkHolder chunk, int x, int y, int z, BiomeType biome);

      <T extends BlockStateHolder<T>> boolean setBlock(ChunkHolder chunk, int x, int y, int z, T holder);

      BiomeType getBiome(ChunkHolder chunk, int x, int y, int z);

      BlockState getBlock(ChunkHolder chunk, int x, int y, int z);

      BaseBlock getFullBlock(ChunkHolder chunk, int x, int y, int z);

      void setSkyLight(ChunkHolder chunk, int x, int y, int z, int value);

      void setBlockLight(ChunkHolder chunk, int x, int y, int z, int value);

      void removeSectionLighting(ChunkHolder chunk, int layer, boolean sky);

      void setFullBright(ChunkHolder chunk, int layer);

      void setLightLayer(ChunkHolder chunk, int layer, char[] toSet);

      void setSkyLightLayer(ChunkHolder chunk, int layer, char[] toSet);

      void setHeightMap(ChunkHolder chunk, HeightMapType type, int[] heightMap);

      int getSkyLight(ChunkHolder chunk, int x, int y, int z);

      int getEmittedLight(ChunkHolder chunk, int x, int y, int z);

      int getBrightness(ChunkHolder chunk, int x, int y, int z);

      int getOpacity(ChunkHolder chunk, int x, int y, int z);

      int[] getHeightMap(ChunkHolder chunk, HeightMapType type);

      void flushLightToGet(ChunkHolder chunk);

      void setLightingToGet(ChunkHolder chunk, char[][] lighting);

      void setSkyLightingToGet(ChunkHolder chunk, char[][] lighting);

      void setHeightmapToGet(ChunkHolder chunk, HeightMapType type, int[] data);
   }
}

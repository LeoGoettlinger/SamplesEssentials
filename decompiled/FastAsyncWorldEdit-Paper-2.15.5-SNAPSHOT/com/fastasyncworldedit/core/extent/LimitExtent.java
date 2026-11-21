package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.extent.filter.block.ExtentFilterBlock;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;

public class LimitExtent extends AbstractDelegateExtent implements IBatchProcessor {
   private final FaweLimit limit;
   private final boolean[] faweExceptionReasonsUsed = new boolean[FaweException.Type.values().length];
   private final Consumer<Component> onErrorMessage;
   private final int chunk_size;
   private boolean processing;

   @Deprecated(
      forRemoval = true,
      since = "2.12.0"
   )
   public LimitExtent(Extent extent, FaweLimit limit) {
      this(extent, limit, c -> {});
   }

   @Deprecated(
      forRemoval = true,
      since = "2.12.0"
   )
   public LimitExtent(Extent extent, FaweLimit limit, Consumer<Component> onErrorMessage) {
      this(extent, limit, onErrorMessage, false);
   }

   public LimitExtent(Extent extent, FaweLimit limit, Consumer<Component> onErrorMessage, boolean processing) {
      super(extent);
      this.limit = limit;
      this.onErrorMessage = onErrorMessage;
      this.chunk_size = 256 * (extent.getMaxY() - extent.getMinY());
      this.processing = processing;
   }

   private void handleException(FaweException e) {
      if (!e.ignorable() && this.limit.MAX_FAILS()) {
         if (!this.faweExceptionReasonsUsed[e.getType().ordinal()]) {
            this.faweExceptionReasonsUsed[e.getType().ordinal()] = true;
            this.onErrorMessage.accept(e.getComponent());
         }
      } else {
         throw e;
      }
   }

   @Override
   public List<? extends Entity> getEntities(Region region) {
      this.limit.THROW_MAX_CHECKS(region.getVolume());

      try {
         return this.extent.getEntities(region);
      } catch (FaweException var3) {
         this.handleException(var3);
         return Collections.emptyList();
      }
   }

   @Override
   public List<? extends Entity> getEntities() {
      this.limit.THROW_MAX_CHECKS();

      try {
         return this.extent.getEntities();
      } catch (FaweException var2) {
         this.handleException(var2);
         return Collections.emptyList();
      }
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      this.limit.THROW_MAX_CHANGES();
      this.limit.THROW_MAX_ENTITIES();

      try {
         return this.extent.createEntity(location, entity);
      } catch (FaweException var4) {
         this.handleException(var4);
         return null;
      }
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      this.limit.THROW_MAX_CHANGES();
      this.limit.THROW_MAX_ENTITIES();

      try {
         return this.extent.createEntity(location, entity, uuid);
      } catch (FaweException var5) {
         this.handleException(var5);
         return null;
      }
   }

   @Override
   public void removeEntity(int x, int y, int z, UUID uuid) {
      this.limit.THROW_MAX_CHANGES();
      this.limit.THROW_MAX_ENTITIES();

      try {
         this.extent.removeEntity(x, y, z, uuid);
      } catch (FaweException var6) {
         this.handleException(var6);
      }
   }

   @Override
   public boolean regenerateChunk(int x, int z, @Nullable BiomeType type, @Nullable Long seed) {
      this.limit.THROW_MAX_CHANGES(this.chunk_size);

      try {
         return this.extent.regenerateChunk(x, z, type, seed);
      } catch (FaweException var6) {
         this.handleException(var6);
         return false;
      }
   }

   @Override
   public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
      this.limit.THROW_MAX_CHECKS(maxY - minY + 1);

      try {
         return this.extent.getHighestTerrainBlock(x, z, minY, maxY);
      } catch (FaweException var6) {
         this.handleException(var6);
         return minY;
      }
   }

   @Override
   public int getHighestTerrainBlock(int x, int z, int minY, int maxY, Mask filter) {
      this.limit.THROW_MAX_CHECKS(maxY - minY + 1);

      try {
         return this.extent.getHighestTerrainBlock(x, z, minY, maxY, filter);
      } catch (FaweException var7) {
         this.handleException(var7);
         return minY;
      }
   }

   @Override
   public int getNearestSurfaceLayer(int x, int z, int y, int minY, int maxY) {
      this.limit.THROW_MAX_CHECKS(maxY - minY + 1);

      try {
         return this.extent.getNearestSurfaceLayer(x, z, y, minY, maxY);
      } catch (FaweException var7) {
         this.handleException(var7);
         return minY;
      }
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, boolean ignoreAir) {
      this.limit.THROW_MAX_CHECKS(maxY - minY + 1);

      try {
         return this.extent.getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, ignoreAir);
      } catch (FaweException var8) {
         this.handleException(var8);
         return minY;
      }
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY) {
      this.limit.THROW_MAX_CHECKS(maxY - minY + 1);

      try {
         return this.extent.getNearestSurfaceTerrainBlock(x, z, y, minY, maxY);
      } catch (FaweException var7) {
         this.handleException(var7);
         return minY;
      }
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, int failedMin, int failedMax) {
      this.limit.THROW_MAX_CHECKS(maxY - minY + 1);

      try {
         return this.extent.getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, failedMin, failedMax);
      } catch (FaweException var9) {
         this.handleException(var9);
         return minY;
      }
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, int failedMin, int failedMax, Mask mask) {
      this.limit.THROW_MAX_CHECKS(maxY - minY + 1);

      try {
         return this.extent.getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, failedMin, failedMax, mask);
      } catch (FaweException var10) {
         this.handleException(var10);
         return minY;
      }
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY, int failedMin, int failedMax, boolean ignoreAir) {
      this.limit.THROW_MAX_CHECKS(maxY - minY + 1);

      try {
         return this.extent.getNearestSurfaceTerrainBlock(x, z, y, minY, maxY, failedMin, failedMax, ignoreAir);
      } catch (FaweException var10) {
         this.handleException(var10);
         return minY;
      }
   }

   @Override
   public List<Countable<BlockType>> getBlockDistribution(Region region) {
      this.limit.THROW_MAX_CHECKS(region.getVolume());
      return this.extent.getBlockDistribution(region);
   }

   @Override
   public List<Countable<BlockState>> getBlockDistributionWithData(Region region) {
      this.limit.THROW_MAX_CHECKS(region.getVolume());
      return this.extent.getBlockDistributionWithData(region);
   }

   @Override
   public int countBlocks(Region region, Set<BaseBlock> searchBlocks) {
      this.limit.THROW_MAX_CHECKS(region.getVolume());
      return this.extent.countBlocks(region, searchBlocks);
   }

   @Override
   public int countBlocks(Region region, Mask searchMask) {
      this.limit.THROW_MAX_CHECKS(region.getVolume());
      return this.extent.countBlocks(region, searchMask);
   }

   @Override
   public <B extends BlockStateHolder<B>> int setBlocks(Region region, B block) throws MaxChangedBlocksException {
      this.limit.THROW_MAX_CHANGES(region.getVolume());
      return this.extent.setBlocks(region, block);
   }

   @Override
   public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
      this.limit.THROW_MAX_CHANGES(region.getVolume());
      return this.extent.setBlocks(region, pattern);
   }

   @Override
   public <B extends BlockStateHolder<B>> int replaceBlocks(Region region, Set<BaseBlock> filter, B replacement) throws MaxChangedBlocksException {
      this.limit.THROW_MAX_CHECKS(region.getVolume());
      this.limit.THROW_MAX_CHANGES(region.getVolume());
      return this.extent.replaceBlocks(region, filter, replacement);
   }

   @Override
   public int replaceBlocks(Region region, Set<BaseBlock> filter, Pattern pattern) throws MaxChangedBlocksException {
      this.limit.THROW_MAX_CHECKS(region.getVolume());
      this.limit.THROW_MAX_CHANGES(region.getVolume());
      return this.extent.replaceBlocks(region, filter, pattern);
   }

   @Override
   public int replaceBlocks(Region region, Mask mask, Pattern pattern) throws MaxChangedBlocksException {
      this.limit.THROW_MAX_CHECKS(region.getVolume());
      this.limit.THROW_MAX_CHANGES(region.getVolume());
      return this.extent.replaceBlocks(region, mask, pattern);
   }

   @Override
   public int setBlocks(Set<BlockVector3> vset, Pattern pattern) {
      this.limit.THROW_MAX_CHANGES(vset.size());
      return this.extent.setBlocks(vset, pattern);
   }

   @Override
   public <T extends Filter> T apply(Region region, T filter, boolean full) {
      this.limit.THROW_MAX_CHECKS(region.getVolume());
      this.limit.THROW_MAX_CHANGES(region.getVolume());
      return this.extent.apply(region, filter, full);
   }

   @Override
   public <T extends Filter> T apply(Iterable<BlockVector3> positions, T filter) {
      int size;
      if (positions instanceof Collection) {
         size = ((Collection)positions).size();
      } else if (positions instanceof Region) {
         BlockVector3 dim = ((Region)positions).getDimensions();
         size = dim.x() * dim.y() * dim.z();
      } else {
         if (!(positions instanceof Extent)) {
            ExtentFilterBlock block = new ExtentFilterBlock(this);

            for (BlockVector3 pos : positions) {
               this.limit.THROW_MAX_CHECKS();

               try {
                  filter.applyBlock(block.init(pos));
               } catch (FaweException var8) {
                  this.handleException(var8);
               }
            }

            return filter;
         }

         BlockVector3 min = ((Extent)positions).getMinimumPoint();
         BlockVector3 max = ((Extent)positions).getMinimumPoint();
         BlockVector3 dim = max.subtract(min).add(BlockVector3.ONE);
         size = dim.x() * dim.y() * dim.z();
      }

      this.limit.THROW_MAX_CHECKS(size);
      this.limit.THROW_MAX_CHANGES(size);
      return this.extent.apply(positions, filter);
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      this.limit.THROW_MAX_CHECKS();

      try {
         return this.extent.getBlock(position);
      } catch (FaweException var3) {
         this.handleException(var3);
         return BlockTypes.AIR.getDefaultState();
      }
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      this.limit.THROW_MAX_CHECKS();

      try {
         return this.extent.getBlock(x, y, z);
      } catch (FaweException var5) {
         this.handleException(var5);
         return BlockTypes.AIR.getDefaultState();
      }
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      this.limit.THROW_MAX_CHECKS();

      try {
         return this.extent.getFullBlock(position);
      } catch (FaweException var3) {
         this.handleException(var3);
         return BlockTypes.AIR.getDefaultState().toBaseBlock();
      }
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      this.limit.THROW_MAX_CHECKS();

      try {
         return this.extent.getFullBlock(x, y, z);
      } catch (FaweException var5) {
         this.handleException(var5);
         return BlockTypes.AIR.getDefaultState().toBaseBlock();
      }
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      this.limit.THROW_MAX_CHECKS();

      try {
         return this.extent.getBiome(position);
      } catch (FaweException var3) {
         this.handleException(var3);
         return BiomeTypes.FOREST;
      }
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      this.limit.THROW_MAX_CHECKS();

      try {
         return this.extent.getBiomeType(x, y, z);
      } catch (FaweException var5) {
         this.handleException(var5);
         return BiomeTypes.FOREST;
      }
   }

   @Deprecated
   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
      this.limit.THROW_MAX_CHANGES();
      if (block.getNbtData() != null || block.getBlockType().getMaterial().isTile()) {
         this.limit.THROW_MAX_BLOCKSTATES();
      }

      try {
         return this.extent.setBlock(position, block);
      } catch (FaweException var4) {
         this.handleException(var4);
         return false;
      }
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      this.limit.THROW_MAX_CHANGES();
      if (block.getNbtData() != null || block.getBlockType().getMaterial().isTile()) {
         this.limit.THROW_MAX_BLOCKSTATES();
      }

      try {
         return this.extent.setBlock(x, y, z, block);
      } catch (FaweException var6) {
         this.handleException(var6);
         return false;
      }
   }

   @Override
   public boolean setTile(int x, int y, int z, CompoundTag tile) throws WorldEditException {
      this.limit.THROW_MAX_CHANGES();
      this.limit.THROW_MAX_BLOCKSTATES();

      try {
         return this.extent.setTile(x, y, z, tile);
      } catch (FaweException var6) {
         this.handleException(var6);
         return false;
      }
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      this.limit.THROW_MAX_CHANGES();

      try {
         return this.extent.setBiome(position, biome);
      } catch (FaweException var4) {
         this.handleException(var4);
         return false;
      }
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      this.limit.THROW_MAX_CHANGES();

      try {
         return this.extent.setBiome(x, y, z, biome);
      } catch (FaweException var6) {
         this.handleException(var6);
         return false;
      }
   }

   public void setProcessing(boolean processing) {
      this.processing = processing;
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      if (!this.processing) {
         return set;
      } else {
         int tiles = set.tiles().size();
         int ents = set.entities().size() + set.getEntityRemoves().size();
         this.limit.THROW_MAX_CHANGES(tiles + ents);
         this.limit.THROW_MAX_BLOCKSTATES(tiles);
         this.limit.THROW_MAX_ENTITIES(ents);
         return set;
      }
   }

   @Override
   public Extent construct(final Extent child) {
      if (this.extent != child) {
         new ExtentTraverser<>(this).setNext(child);
      }

      return this;
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.READING_SET_BLOCKS;
   }
}

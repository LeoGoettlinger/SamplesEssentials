package com.sk89q.worldedit.extent;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.HistoryExtent;
import com.fastasyncworldedit.core.history.changeset.AbstractChangeSet;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.nbt.FaweCompoundTag;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.util.ExtentTraverser;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.buffer.ForgetfulExtentBuffer;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class AbstractDelegateExtent implements Extent {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   public Extent extent;

   public AbstractDelegateExtent(Extent extent) {
      Preconditions.checkNotNull(extent);
      this.extent = extent;
   }

   public Extent getExtent() {
      return this.extent;
   }

   @Override
   public BlockState getBlock(BlockVector3 position) {
      return this.extent.getBlock(position);
   }

   @Override
   public BlockState getBlock(int x, int y, int z) {
      return this.extent.getBlock(x, y, z);
   }

   @Override
   public BaseBlock getFullBlock(BlockVector3 position) {
      return this.extent.getFullBlock(position);
   }

   @Override
   public BaseBlock getFullBlock(int x, int y, int z) {
      return this.extent.getFullBlock(x, y, z);
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      return this.extent.getMinimumPoint();
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      return this.extent.getMaximumPoint();
   }

   @Override
   public List<? extends Entity> getEntities(Region region) {
      return this.extent.getEntities(region);
   }

   @Override
   public List<? extends Entity> getEntities() {
      return this.extent.getEntities();
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity) {
      return this.extent.createEntity(location, entity);
   }

   @Nullable
   @Override
   public Entity createEntity(Location location, BaseEntity entity, UUID uuid) {
      return this.extent.createEntity(location, entity, uuid);
   }

   @Nullable
   @Override
   public Operation commit() {
      Operation ours = this.commitBefore();
      Operation other = null;
      if (this.extent != this) {
         other = this.extent.commit();
      }

      if (ours != null && other != null) {
         return new OperationQueue(ours, other);
      } else {
         return ours != null ? ours : other;
      }
   }

   @Override
   public boolean cancel() {
      ExtentTraverser<Extent> traverser = new ExtentTraverser<>(this);
      com.fastasyncworldedit.core.extent.NullExtent nullExtent = new com.fastasyncworldedit.core.extent.NullExtent(this.getExtent(), FaweCache.MANUAL);
      ExtentTraverser<Extent> next = traverser.next();
      if (next != null) {
         Extent child = next.get();
         if (child instanceof com.fastasyncworldedit.core.extent.NullExtent) {
            return true;
         }

         traverser.setNext(nullExtent);
         child.cancel();
      }

      this.addProcessor(nullExtent);
      this.addPostProcessor(nullExtent);
      return true;
   }

   @Override
   public void removeEntity(int x, int y, int z, UUID uuid) {
      this.extent.removeEntity(x, y, z, uuid);
   }

   @Override
   public boolean isQueueEnabled() {
      return this.extent.isQueueEnabled();
   }

   @Override
   public void enableQueue() {
      try {
         this.extent.enableQueue();
      } catch (FaweException var2) {
         new ExtentTraverser<>(this).setNext(new ForgetfulExtentBuffer(this.extent));
      }
   }

   @Override
   public void disableQueue() {
      try {
         if (!(this.extent instanceof ForgetfulExtentBuffer)) {
            this.extent.disableQueue();
         }
      } catch (FaweException var2) {
      }

      if (this.extent instanceof AbstractDelegateExtent) {
         Extent next = ((AbstractDelegateExtent)this.extent).getExtent();
         new ExtentTraverser<>(this).setNext(next);
      } else {
         LOGGER.error("Cannot disable queue");
      }
   }

   @Override
   public boolean isWorld() {
      return this.extent.isWorld();
   }

   @Override
   public List<Countable<BlockType>> getBlockDistribution(final Region region) {
      return this.extent.getBlockDistribution(region);
   }

   @Override
   public List<Countable<BlockState>> getBlockDistributionWithData(final Region region) {
      return this.extent.getBlockDistributionWithData(region);
   }

   @Override
   public int getMaxY() {
      return this.extent.getMaxY();
   }

   @Override
   public int countBlocks(final Region region, final Set<BaseBlock> searchBlocks) {
      return this.extent.countBlocks(region, searchBlocks);
   }

   @Override
   public int countBlocks(final Region region, final Mask searchMask) {
      return this.extent.countBlocks(region, searchMask);
   }

   @Override
   public <B extends BlockStateHolder<B>> int setBlocks(final Region region, final B block) throws MaxChangedBlocksException {
      return this.extent.setBlocks(region, block);
   }

   @Override
   public int setBlocks(final Region region, final Pattern pattern) throws MaxChangedBlocksException {
      return this.extent.setBlocks(region, pattern);
   }

   @Override
   public <B extends BlockStateHolder<B>> int replaceBlocks(final Region region, final Set<BaseBlock> filter, final B replacement) throws MaxChangedBlocksException {
      return this.extent.replaceBlocks(region, filter, replacement);
   }

   @Override
   public int replaceBlocks(final Region region, final Set<BaseBlock> filter, final Pattern pattern) throws MaxChangedBlocksException {
      return this.extent.replaceBlocks(region, filter, pattern);
   }

   @Override
   public int replaceBlocks(final Region region, final Mask mask, final Pattern pattern) throws MaxChangedBlocksException {
      return this.extent.replaceBlocks(region, mask, pattern);
   }

   @Override
   public int setBlocks(final Set<BlockVector3> vset, final Pattern pattern) {
      return this.extent.setBlocks(vset, pattern);
   }

   @Override
   public int getMinY() {
      return this.extent.getMinY();
   }

   @Override
   public boolean relight(int x, int y, int z) {
      return this.extent.relight(x, y, z);
   }

   @Override
   public boolean relightBlock(int x, int y, int z) {
      return this.extent.relightBlock(x, y, z);
   }

   @Override
   public boolean relightSky(int x, int y, int z) {
      return this.extent.relightSky(x, y, z);
   }

   @Override
   public Extent addProcessor(IBatchProcessor processor) {
      if (Settings.settings().EXPERIMENTAL.OTHER) {
         LOGGER.info("addProcessor Info: \t " + processor.getClass().getName());
         LOGGER.info("The following is not an error or a crash:");
         StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

         for (StackTraceElement stackTraceElement : stackTrace) {
            LOGGER.info(stackTraceElement.toString());
         }
      }

      Extent result = this.extent.addProcessor(processor);
      if (result != this.extent) {
         new ExtentTraverser<>(this).setNext(result);
      }

      return this;
   }

   @Override
   public Extent addPostProcessor(IBatchProcessor processor) {
      if (Settings.settings().EXPERIMENTAL.OTHER) {
         LOGGER.info("addPostProcessor Info: \t " + processor.getClass().getName());
         LOGGER.info("The following is not an error or a crash:");
         StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

         for (StackTraceElement stackTraceElement : stackTrace) {
            LOGGER.info(stackTraceElement.toString());
         }
      }

      Extent result = this.extent.addPostProcessor(processor);
      if (result != this.extent) {
         new ExtentTraverser<>(this).setNext(result);
      }

      return this;
   }

   @Override
   public Extent disableHistory() {
      Extent result = this.extent.disableHistory();
      if (result != this.extent) {
         new ExtentTraverser<>(this).setNext(result);
      }

      return this;
   }

   @Override
   public <T extends Filter> T apply(final Region region, final T filter, final boolean full) {
      return this.extent.apply(region, filter, full);
   }

   protected Operation commitBefore() {
      return null;
   }

   @Override
   public BiomeType getBiome(BlockVector3 position) {
      return this.extent.getBiome(position);
   }

   @Override
   public BiomeType getBiomeType(int x, int y, int z) {
      return this.extent.getBiomeType(x, y, z);
   }

   @Override
   public int getEmittedLight(int x, int y, int z) {
      return this.extent.getEmittedLight(x, y, z);
   }

   @Override
   public int getSkyLight(int x, int y, int z) {
      return this.extent.getSkyLight(x, y, z);
   }

   @Override
   public int getBrightness(int x, int y, int z) {
      return this.extent.getBrightness(x, y, z);
   }

   public void setChangeSet(AbstractChangeSet changeSet) {
      if (this.extent instanceof HistoryExtent history) {
         if (changeSet == null) {
            new ExtentTraverser<>(this).setNext(history.getExtent());
         } else {
            history.setChangeSet(changeSet);
         }
      } else if (this.extent instanceof AbstractDelegateExtent) {
         ((AbstractDelegateExtent)this.extent).setChangeSet(changeSet);
      } else if (changeSet != null) {
         new ExtentTraverser<>(this).setNext(new HistoryExtent(this.extent, changeSet));
      }
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
      return this.extent.setBlock(position, block);
   }

   @Override
   public <T extends BlockStateHolder<T>> boolean setBlock(int x, int y, int z, T block) throws WorldEditException {
      return this.extent.setBlock(x, y, z, block);
   }

   @Override
   public boolean tile(int x, int y, int z, FaweCompoundTag tile) throws WorldEditException {
      return this.setBlock(x, y, z, this.getBlock(x, y, z).toBaseBlock(tile == null ? null : tile.linTag()));
   }

   @Override
   public boolean fullySupports3DBiomes() {
      return this.extent.fullySupports3DBiomes();
   }

   @Override
   public boolean setBiome(BlockVector3 position, BiomeType biome) {
      return this.extent.setBiome(position, biome);
   }

   @Override
   public boolean setBiome(int x, int y, int z, BiomeType biome) {
      return this.extent.setBiome(x, y, z, biome);
   }

   @Override
   public void setBlockLight(int x, int y, int z, int value) {
      this.extent.setSkyLight(x, y, z, value);
   }

   @Override
   public void setSkyLight(int x, int y, int z, int value) {
      this.extent.setSkyLight(x, y, z, value);
   }

   @Override
   public String toString() {
      return super.toString() + ":" + (this.extent == this ? "" : this.extent.toString());
   }
}

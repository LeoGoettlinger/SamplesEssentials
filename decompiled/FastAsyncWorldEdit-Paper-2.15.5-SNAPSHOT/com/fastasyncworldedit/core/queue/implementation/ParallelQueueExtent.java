package com.fastasyncworldedit.core.queue.implementation;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.configuration.Settings;
import com.fastasyncworldedit.core.extent.NullExtent;
import com.fastasyncworldedit.core.extent.PassthroughExtent;
import com.fastasyncworldedit.core.extent.clipboard.WorldCopyClipboard;
import com.fastasyncworldedit.core.extent.filter.CountFilter;
import com.fastasyncworldedit.core.extent.filter.DistrFilter;
import com.fastasyncworldedit.core.extent.filter.LinkedFilter;
import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.extent.processor.BatchProcessorHolder;
import com.fastasyncworldedit.core.extent.processor.MultiBatchProcessor;
import com.fastasyncworldedit.core.function.mask.BlockMaskBuilder;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.internal.simd.SimdSupport;
import com.fastasyncworldedit.core.internal.simd.VectorizedFilter;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IQueueChunk;
import com.fastasyncworldedit.core.queue.IQueueExtent;
import com.fastasyncworldedit.core.util.task.FaweThreadUtil;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import javax.annotation.Nullable;
import org.apache.logging.log4j.Logger;

public class ParallelQueueExtent extends PassthroughExtent {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private final World world;
   private final QueueHandler handler;
   private final BatchProcessorHolder processor;
   private final BatchProcessorHolder postProcessor;
   private final boolean[] faweExceptionReasonsUsed = new boolean[FaweException.Type.values().length];
   private final boolean fastmode;
   private final SideEffectSet sideEffectSet;
   private int changes;

   public ParallelQueueExtent(QueueHandler handler, World world, boolean fastmode, @Nullable SideEffectSet sideEffectSet) {
      super(handler.getQueue(world, new BatchProcessorHolder(), new BatchProcessorHolder()));
      this.world = world;
      this.handler = handler;
      this.processor = (BatchProcessorHolder)this.getExtent().getProcessor();
      if (this.processor.getProcessor() instanceof MultiBatchProcessor) {
         ((MultiBatchProcessor)this.processor.getProcessor()).setFaweExceptionArray(this.faweExceptionReasonsUsed);
      }

      this.postProcessor = (BatchProcessorHolder)this.getExtent().getPostProcessor();
      if (this.postProcessor.getProcessor() instanceof MultiBatchProcessor) {
         ((MultiBatchProcessor)this.postProcessor.getProcessor()).setFaweExceptionArray(this.faweExceptionReasonsUsed);
      }

      this.fastmode = fastmode;
      this.sideEffectSet = sideEffectSet == null ? SideEffectSet.defaults() : sideEffectSet;
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   public static void clearCurrentExtent() {
      FaweThreadUtil.clearCurrentExtent();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.13.0"
   )
   public static void setCurrentExtent(Extent extent) {
      FaweThreadUtil.setCurrentExtent(extent);
   }

   void enter(Extent extent) {
      FaweThreadUtil.setCurrentExtent(extent);
   }

   void exit() {
      FaweThreadUtil.clearCurrentExtent();
   }

   public IQueueExtent<IQueueChunk> getExtent() {
      Extent extent = FaweThreadUtil.getCurrentExtent();
      if (extent == null) {
         extent = super.getExtent();
      }

      return (IQueueExtent<IQueueChunk>)extent;
   }

   @Override
   public boolean cancel() {
      if (super.cancel()) {
         this.processor.setProcessor(new NullExtent(this, FaweCache.MANUAL));
         this.postProcessor.setPostProcessor(new NullExtent(this, FaweCache.MANUAL));
         return true;
      } else {
         return false;
      }
   }

   IQueueExtent<IQueueChunk> getNewQueue() {
      SingleThreadQueueExtent queue = (SingleThreadQueueExtent)this.handler.getQueue(this.world, this.processor, this.postProcessor);
      queue.setFastMode(this.fastmode);
      queue.setSideEffectSet(this.sideEffectSet);
      queue.setFaweExceptionArray(this.faweExceptionReasonsUsed);
      queue.setTargetSize(Settings.settings().QUEUE.TARGET_SIZE * Settings.settings().QUEUE.THREAD_TARGET_SIZE_PERCENT / 100);
      return queue;
   }

   @Override
   public <T extends Filter> T apply(Region region, T filter, boolean full) {
      Set<BlockVector2> chunks = region.getChunks();
      Iterator<BlockVector2> chunksIter = chunks.iterator();
      int size = Math.min(chunks.size(), Settings.settings().QUEUE.PARALLEL_THREADS);
      if (size <= 1) {
         ChunkFilterBlock block = null;

         while (chunksIter.hasNext()) {
            BlockVector2 pos = chunksIter.next();
            block = this.getExtent().apply(block, filter, region, pos.x(), pos.z(), full);
         }

         this.getExtent().flush();
         filter.finish();
      } else {
         ForkJoinTask<?> task = this.handler.submit(new ApplyTask<>(region, filter, this, full, this.faweExceptionReasonsUsed));

         try {
            task.join();
         } catch (Throwable var9) {
            LOGGER.catching(var9);
         }

         filter.join();
      }

      return filter;
   }

   @Override
   protected Operation commitBefore() {
      return new Operation() {
         @Override
         public Operation resume(final RunContext run) throws WorldEditException {
            ParallelQueueExtent.this.extent.commit();
            ParallelQueueExtent.this.processor.flush();
            ((IQueueExtent)ParallelQueueExtent.this.extent).flush();
            return null;
         }

         @Override
         public void cancel() {
         }
      };
   }

   @Override
   public int countBlocks(Region region, Mask searchMask) {
      return this.apply(region, searchMask.toFilter(new CountFilter()), searchMask.replacesAir()).getParent().getTotal();
   }

   @Override
   public <B extends BlockStateHolder<B>> int setBlocks(Region region, B block) throws MaxChangedBlocksException {
      Mask mask = new BlockMaskBuilder().add(block).build(this).inverse();
      return this.changes = this.apply(region, mask.toFilter(block), mask.replacesAir()).getBlocksApplied();
   }

   @Override
   public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
      VectorizedFilter vectorizedPattern = SimdSupport.vectorizedPattern(pattern);
      LinkedFilter<?, ? extends CountFilter> filter = LinkedFilter.of((Filter)(vectorizedPattern == null ? pattern : vectorizedPattern), new CountFilter());
      return this.changes = this.apply(region, filter, true).getRight().getTotal();
   }

   @Override
   public int setBlocks(Set<BlockVector3> vset, Pattern pattern) {
      if (vset instanceof Region) {
         this.changes = this.setBlocks((Region)vset, pattern);
         return this.changes;
      } else {
         for (BlockVector3 blockVector3 : vset) {
            if (pattern.apply(this, blockVector3, blockVector3)) {
               this.changes++;
            }
         }

         return this.changes;
      }
   }

   @Override
   public int replaceBlocks(Region region, Mask mask, Pattern pattern) throws MaxChangedBlocksException {
      boolean full = mask.replacesAir();
      return this.changes = this.apply(region, mask.toFilter(pattern), full).getBlocksApplied();
   }

   @Override
   public List<Countable<BlockState>> getBlockDistributionWithData(Region region) {
      return this.apply(region, new DistrFilter(), true).getDistribution();
   }

   @Override
   public List<Countable<BlockType>> getBlockDistribution(Region region) {
      return this.apply(region, new DistrFilter(), true).getTypeDistribution();
   }

   @Override
   public Clipboard lazyCopy(Region region) {
      Clipboard clipboard = WorldCopyClipboard.of(this, region);
      clipboard.setOrigin(region.getMinimumPoint());
      return clipboard;
   }

   @Override
   public int countBlocks(Region region, Set<BaseBlock> searchBlocks) {
      Mask mask = new BlockMask(this, searchBlocks);
      return this.countBlocks(region, mask);
   }

   @Override
   public <B extends BlockStateHolder<B>> int replaceBlocks(Region region, Set<BaseBlock> filter, B replacement) throws MaxChangedBlocksException {
      return this.replaceBlocks(region, filter, new BlockPattern(replacement));
   }

   @Override
   public int replaceBlocks(Region region, Set<BaseBlock> filter, Pattern pattern) throws MaxChangedBlocksException {
      Mask mask = (Mask)(filter == null ? new ExistingBlockMask(this) : new BlockMask(this, filter));
      return this.replaceBlocks(region, mask, pattern);
   }
}

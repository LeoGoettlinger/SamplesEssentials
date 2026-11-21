package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.extent.processor.IBatchProcessorHolder;
import com.fastasyncworldedit.core.internal.simd.SimdSupport;
import com.fastasyncworldedit.core.internal.simd.VectorizedCharFilterBlock;
import com.fastasyncworldedit.core.internal.simd.VectorizedFilter;
import com.fastasyncworldedit.core.queue.implementation.chunk.ChunkHolder;
import com.fastasyncworldedit.core.queue.implementation.chunk.WrapperChunk;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.SideEffectSet;
import java.io.Flushable;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javax.annotation.Nullable;
import org.jetbrains.annotations.ApiStatus.Internal;

public interface IQueueExtent<T extends IChunk> extends Flushable, Trimable, IChunkExtent<T>, IBatchProcessorHolder {
   @Override
   default boolean isQueueEnabled() {
      return true;
   }

   @Override
   void enableQueue();

   @Override
   void disableQueue();

   void init(Extent extent, IChunkCache<IChunkGet> get, IChunkCache<IChunkSet> set);

   IChunkGet getCachedGet(int chunkX, int chunkZ);

   IChunkSet getCachedSet(int chunkX, int chunkZ);

   <V extends Future<V>> V submit(T chunk);

   @Override
   default BlockVector3 getMinimumPoint() {
      return BlockVector3.at(-30000000, this.getMinY(), -30000000);
   }

   @Override
   default BlockVector3 getMaximumPoint() {
      return BlockVector3.at(30000000, this.getMaxY(), 30000000);
   }

   void setFastMode(boolean fastMode);

   boolean isFastMode();

   void setSideEffectSet(SideEffectSet sideEffectSet);

   SideEffectSet getSideEffectSet();

   @Internal
   <V extends Future<V>> V submitTaskUnchecked(Callable<V> callable);

   T create(boolean isFull);

   default T wrap(T root) {
      return root;
   }

   @Nullable
   @Override
   default Operation commit() {
      this.flush();
      return null;
   }

   @Override
   void flush();

   ChunkFilterBlock createFilterBlock();

   int size();

   boolean isEmpty();

   default ChunkFilterBlock apply(@Nullable ChunkFilterBlock block, Filter filter, Region region, int chunkX, int chunkZ, boolean full) {
      T initial = this.getOrCreateChunk(chunkX, chunkZ);
      WrapperChunk<T> chunk = new WrapperChunk<>(initial, () -> this.getOrCreateChunk(chunkX, chunkZ));
      if (initial instanceof ChunkHolder<?> holder) {
         holder.setWrapper(chunk);
      }

      IChunk newChunk = filter.applyChunk(chunk, region);
      if (newChunk == chunk) {
         newChunk = chunk.get();
      } else {
         chunk.setWrapped((T)newChunk);
         if (newChunk instanceof ChunkHolder<?> holder) {
            holder.setWrapper(chunk);
         }
      }

      if (newChunk != null) {
         if (block == null) {
            if (SimdSupport.useVectorApi() && filter instanceof VectorizedFilter) {
               block = new VectorizedCharFilterBlock(this);
            } else {
               block = this.createFilterBlock();
            }
         }

         block.initChunk(chunkX, chunkZ);
         chunk.filterBlocks(filter, block, region, full);
      }

      T toSubmit = chunk.get();
      if (toSubmit != null) {
         this.submit(toSubmit);
      }

      return block;
   }

   @Override
   default <U extends Filter> U apply(Region region, U filter, boolean full) {
      Set<BlockVector2> chunks = region.getChunks();
      ChunkFilterBlock block = null;

      for (BlockVector2 chunk : chunks) {
         block = this.apply(block, filter, region, chunk.x(), chunk.z(), full);
      }

      this.flush();
      return filter;
   }
}

package com.fastasyncworldedit.core.queue.implementation;

import com.fastasyncworldedit.core.Fawe;
import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.internal.exception.FaweException;
import com.fastasyncworldedit.core.queue.Filter;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import org.apache.logging.log4j.Logger;

class ApplyTask<F extends Filter> extends RecursiveAction implements Runnable {
   private static final Logger LOGGER = LogManagerCompat.getLogger();
   private static final int INITIAL_REGION_SHIFT = 5;
   private static final int SHIFT_REDUCTION = 1;
   private final ApplyTask.CommonState<F> commonState;
   private final Region region;
   private final ApplyTask<F> before;
   private final int minChunkX;
   private final int minChunkZ;
   private final int maxChunkX;
   private final int maxChunkZ;
   private final int shift;

   @Override
   public void run() {
      this.compute();
   }

   ApplyTask(final Region region, final F filter, final ParallelQueueExtent parallelQueueExtent, final boolean full, final boolean[] faweExceptionReasonsUsed) {
      this.commonState = new ApplyTask.CommonState<>(filter, parallelQueueExtent, new ConcurrentHashMap<>(), full, faweExceptionReasonsUsed);
      this.region = region.clone();
      this.before = null;
      BlockVector3 minimumPoint = region.getMinimumPoint();
      this.minChunkX = minimumPoint.x() >> 4;
      this.minChunkZ = minimumPoint.z() >> 4;
      BlockVector3 maximumPoint = region.getMaximumPoint();
      this.maxChunkX = maximumPoint.x() >> 4;
      this.maxChunkZ = maximumPoint.z() >> 4;
      this.shift = 5;
   }

   private ApplyTask(
      final ApplyTask.CommonState<F> commonState,
      final Region region,
      final ApplyTask<F> before,
      final int minChunkX,
      final int maxChunkX,
      final int minChunkZ,
      final int maxChunkZ,
      final int higherShift
   ) {
      this.commonState = commonState;
      this.region = region.clone();
      this.minChunkX = minChunkX;
      this.maxChunkX = maxChunkX;
      this.minChunkZ = minChunkZ;
      this.maxChunkZ = maxChunkZ;
      this.before = before;
      this.shift = Math.max(0, higherShift - 1);
   }

   @Override
   protected void compute() {
      if (this.minChunkX == this.maxChunkX && this.minChunkZ == this.maxChunkZ) {
         this.processChunk(this.minChunkX, this.minChunkZ);
      } else {
         ApplyTask<F> subtask = null;
         int minRegionX = this.minChunkX >> this.shift;
         int minRegionZ = this.minChunkZ >> this.shift;
         int maxRegionX = this.maxChunkX >> this.shift;
         int maxRegionZ = this.maxChunkZ >> this.shift;

         for (int regionX = minRegionX; regionX <= maxRegionX; regionX++) {
            for (int regionZ = minRegionZ; regionZ <= maxRegionZ; regionZ++) {
               if (this.shouldProcessDirectly()) {
                  this.processRegion(regionX, regionZ, this.shift);
               } else if (this.shift != 0 || this.region.containsChunk(regionX, regionZ)) {
                  subtask = new ApplyTask<>(
                     this.commonState,
                     this.region,
                     subtask,
                     regionX << this.shift,
                     (regionX + 1 << this.shift) - 1,
                     regionZ << this.shift,
                     (regionZ + 1 << this.shift) - 1,
                     this.shift
                  );
                  subtask.fork();
               }
            }
         }

         for (; subtask != null; subtask = subtask.before) {
            if (subtask.tryUnfork()) {
               subtask.invoke();
            } else {
               subtask.join();
            }
         }
      }

      if (this.shift == 5) {
         this.onCompletion();
      }
   }

   private boolean shouldProcessDirectly() {
      return ForkJoinTask.getSurplusQueuedTaskCount() > Math.max(3, 1 << this.shift);
   }

   private void processRegion(int regionX, int regionZ, int shift) {
      ApplyTask.ThreadState<F> state = this.getState();
      this.commonState.parallelQueueExtent.enter(state.queue);

      try {
         for (int chunkX = regionX << shift; chunkX <= (regionX + 1 << shift) - 1; chunkX++) {
            for (int chunkZ = regionZ << shift; chunkZ <= (regionZ + 1 << shift) - 1; chunkZ++) {
               if (this.region.containsChunk(chunkX, chunkZ)) {
                  this.applyChunk(chunkX, chunkZ, state);
               }
            }
         }
      } finally {
         this.commonState.parallelQueueExtent.exit();
      }
   }

   private ApplyTask.ThreadState<F> getState() {
      return this.commonState
         .stateCache
         .computeIfAbsent(
            Thread.currentThread(),
            __ -> new ApplyTask.ThreadState<>(
               (SingleThreadQueueExtent)this.commonState.parallelQueueExtent.getNewQueue(), (F)this.commonState.originalFilter.fork()
            )
         );
   }

   private void processChunk(int chunkX, int chunkZ) {
      ApplyTask.ThreadState<F> state = this.getState();
      this.commonState.parallelQueueExtent.enter(state.queue);

      try {
         this.applyChunk(chunkX, chunkZ, state);
      } finally {
         this.commonState.parallelQueueExtent.exit();
      }
   }

   private void applyChunk(int chunkX, int chunkZ, ApplyTask.ThreadState<F> state) {
      try {
         state.block = state.queue.apply(state.block, state.filter, this.region, chunkX, chunkZ, this.commonState.full);
      } catch (Throwable var8) {
         if (var8 instanceof FaweException faweException) {
            Fawe.handleFaweException(this.commonState.faweExceptionReasonsUsed, faweException, LOGGER);
         } else {
            if (!(var8.getCause() instanceof FaweException faweException)) {
               throw var8;
            }

            Fawe.handleFaweException(this.commonState.faweExceptionReasonsUsed, faweException, LOGGER);
         }
      }
   }

   private void onCompletion() {
      for (ForkJoinTask<?> task : this.flushQueues()) {
         if (task.tryUnfork()) {
            task.invoke();
         } else {
            task.join();
         }
      }
   }

   private ForkJoinTask<?>[] flushQueues() {
      Collection<ApplyTask.ThreadState<F>> values = this.commonState.stateCache.values();
      ForkJoinTask<?>[] tasks = new ForkJoinTask[values.size()];
      int i = values.size() - 1;

      for (ApplyTask.ThreadState<F> value : values) {
         tasks[i] = ForkJoinTask.adapt(value.queue::flush).fork();
         i--;
      }

      return tasks;
   }

   private record CommonState<F extends Filter>(
      F originalFilter,
      ParallelQueueExtent parallelQueueExtent,
      ConcurrentMap<Thread, ApplyTask.ThreadState<F>> stateCache,
      boolean full,
      boolean[] faweExceptionReasonsUsed
   ) {
   }

   private static final class ThreadState<F extends Filter> {
      private final SingleThreadQueueExtent queue;
      private final F filter;
      private ChunkFilterBlock block;

      private ThreadState(SingleThreadQueueExtent queue, F filter) {
         this.queue = queue;
         this.filter = filter;
      }
   }
}

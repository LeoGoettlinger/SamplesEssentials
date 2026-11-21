package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.queue.implementation.chunk.WrapperChunk;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public interface IQueueChunk<T extends Future<T>> extends IChunk, Callable<T> {
   default IQueueChunk<T> reset() {
      this.init(null, this.getX(), this.getZ());
      return this;
   }

   void setWrapper(WrapperChunk<?> parentWrapper);

   void invalidateWrapper();

   T call();

   default void join() throws ExecutionException, InterruptedException {
      T future = this.call();

      while (future != null) {
         future = future.get();
      }
   }

   default boolean hasRunning() {
      return false;
   }

   default void lockSet() {
   }
}

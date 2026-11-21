package com.fastasyncworldedit.core.extent.processor;

import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.extent.Extent;
import java.util.concurrent.Future;

public interface IBatchProcessorHolder extends IBatchProcessor {
   IBatchProcessor getProcessor();

   IBatchProcessor getPostProcessor();

   void setProcessor(IBatchProcessor set);

   void setPostProcessor(IBatchProcessor set);

   @Override
   default IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      return this.getProcessor().processSet(chunk, get, set);
   }

   @Override
   default Future<?> postProcessSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      return this.getPostProcessor().postProcessSet(chunk, get, set);
   }

   @Override
   default void postProcess(IChunk chunk, IChunkGet get, IChunkSet set) {
      this.getPostProcessor().postProcess(chunk, get, set);
   }

   @Override
   default boolean processGet(int chunkX, int chunkZ) {
      return this.getProcessor().processGet(chunkX, chunkZ);
   }

   @Override
   default Extent construct(Extent child) {
      return this.getProcessor().construct(child);
   }

   @Override
   default IBatchProcessor join(IBatchProcessor other) {
      this.setProcessor(this.getProcessor().join(other));
      return this;
   }

   @Override
   default IBatchProcessor joinPost(IBatchProcessor other) {
      this.setPostProcessor(this.getPostProcessor().joinPost(other));
      return this;
   }

   @Override
   default <T extends IBatchProcessor> IBatchProcessor remove(Class<T> clazz) {
      this.setProcessor(this.getProcessor().remove(clazz));
      return this;
   }
}

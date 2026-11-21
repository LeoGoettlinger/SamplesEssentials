package com.fastasyncworldedit.core.extent.processor;

import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import java.util.concurrent.Future;

public class BatchProcessorHolder implements IBatchProcessorHolder {
   private IBatchProcessor processor = EmptyBatchProcessor.getInstance();
   private IBatchProcessor postProcessor = EmptyBatchProcessor.getInstance();

   @Override
   public IBatchProcessor getProcessor() {
      return this.processor;
   }

   @Override
   public IBatchProcessor getPostProcessor() {
      return this.postProcessor;
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      return this.getProcessor().processSet(chunk, get, set);
   }

   @Override
   public Future<?> postProcessSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      return this.getPostProcessor().postProcessSet(chunk, get, set);
   }

   @Override
   public void postProcess(IChunk chunk, IChunkGet get, IChunkSet set) {
      this.getPostProcessor().postProcess(chunk, get, set);
   }

   @Override
   public boolean processGet(final int chunkX, final int chunkZ) {
      return this.getProcessor().processGet(chunkX, chunkZ);
   }

   @Override
   public IChunkGet processGet(final IChunkGet get) {
      return this.getProcessor().processGet(get);
   }

   @Override
   public void flush() {
      this.getProcessor().flush();
   }

   @Override
   public void setProcessor(IBatchProcessor set) {
      this.processor = set;
   }

   @Override
   public void setPostProcessor(IBatchProcessor set) {
      this.postProcessor = set;
   }

   @Override
   public String toString() {
      IBatchProcessor tmp = this.getProcessor();
      return super.toString() + "{" + (tmp == this ? "" : this.getProcessor()) + "}";
   }

   @Override
   public ProcessorScope getScope() {
      return this.getProcessor().getScope();
   }
}

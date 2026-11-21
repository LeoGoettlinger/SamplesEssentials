package com.fastasyncworldedit.core.extent.processor;

import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.extent.Extent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class EmptyBatchProcessor implements IBatchProcessor {
   private static final EmptyBatchProcessor instance = new EmptyBatchProcessor();

   public static EmptyBatchProcessor getInstance() {
      return instance;
   }

   @Nonnull
   @Override
   public Extent construct(@Nullable Extent child) {
      return child;
   }

   @Nonnull
   @Override
   public IChunkSet processSet(@Nullable IChunk chunk, @Nullable IChunkGet get, @Nullable IChunkSet set) {
      return set;
   }

   @Nonnull
   @Override
   public IBatchProcessor join(@Nullable IBatchProcessor other) {
      return other;
   }

   @Nonnull
   @Override
   public IBatchProcessor joinPost(@Nullable IBatchProcessor other) {
      return other;
   }

   private EmptyBatchProcessor() {
   }

   @Override
   public ProcessorScope getScope() {
      return ProcessorScope.ADDING_BLOCKS;
   }
}

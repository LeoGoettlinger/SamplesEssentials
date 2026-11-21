package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.sk89q.worldedit.regions.Region;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Filter {
   @Nonnull
   default <U extends IChunk> U applyChunk(U chunk, @Nullable Region region) {
      return chunk;
   }

   default void applyBlock(FilterBlock block) {
   }

   default void finishChunk(IChunk chunk) {
   }

   default Filter fork() {
      return this;
   }

   default void join() {
   }

   default void finish() {
   }
}

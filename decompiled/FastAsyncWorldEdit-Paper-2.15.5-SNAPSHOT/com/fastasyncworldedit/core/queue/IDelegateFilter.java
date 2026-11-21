package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.sk89q.worldedit.regions.Region;
import javax.annotation.Nullable;

public interface IDelegateFilter extends Filter {
   Filter getParent();

   @Override
   default <V extends IChunk> V applyChunk(V chunk, @Nullable Region region) {
      return this.getParent().applyChunk(chunk, region);
   }

   @Override
   default void applyBlock(FilterBlock block) {
      this.getParent().applyBlock(block);
   }

   @Override
   default void finishChunk(IChunk chunk) {
      this.getParent().finishChunk(chunk);
   }

   @Override
   default void join() {
      this.getParent().join();
   }

   @Override
   default Filter fork() {
      Filter fork = this.getParent().fork();
      return (Filter)(fork != this.getParent() ? this.newInstance(fork) : this);
   }

   @Override
   default void finish() {
      this.getParent().finish();
   }

   Filter newInstance(Filter other);
}

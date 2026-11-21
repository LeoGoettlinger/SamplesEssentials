package com.sk89q.worldedit.function.pattern;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.queue.Filter;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

public interface Pattern extends Filter {
   @Deprecated
   @NonAbstractForCompatibility(
      delegateName = "applyBlock",
      delegateParams = {BlockVector3.class}
   )
   default BaseBlock apply(BlockVector3 position) {
      return this.applyBlock(position);
   }

   default boolean apply(Extent extent, BlockVector3 get, BlockVector3 set) throws WorldEditException {
      return set.setFullBlock(extent, this.applyBlock(get));
   }

   @Override
   default void applyBlock(final FilterBlock block) {
      this.apply(block, block, block);
   }

   default Pattern fork() {
      return this;
   }

   BaseBlock applyBlock(BlockVector3 position);

   default BlockVector3 size() {
      return BlockVector3.ONE;
   }
}

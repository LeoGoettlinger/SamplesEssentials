package com.sk89q.worldedit.function;

import com.fastasyncworldedit.core.extent.filter.block.FilterBlock;
import com.fastasyncworldedit.core.queue.Filter;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;

public interface RegionFunction extends Filter {
   boolean apply(BlockVector3 position) throws WorldEditException;

   @Override
   default void applyBlock(FilterBlock block) {
      this.apply(block);
   }
}

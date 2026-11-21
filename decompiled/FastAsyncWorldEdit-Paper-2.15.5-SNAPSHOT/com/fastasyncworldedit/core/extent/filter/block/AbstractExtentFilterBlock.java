package com.fastasyncworldedit.core.extent.filter.block;

import com.sk89q.worldedit.extent.Extent;

public abstract class AbstractExtentFilterBlock extends FilterBlock {
   private final Extent extent;

   public AbstractExtentFilterBlock(Extent extent) {
      this.extent = extent;
   }

   @Override
   public final Extent getExtent() {
      return this.extent;
   }
}

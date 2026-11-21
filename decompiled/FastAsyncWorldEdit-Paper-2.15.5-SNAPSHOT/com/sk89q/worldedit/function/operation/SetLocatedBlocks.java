package com.sk89q.worldedit.function.operation;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.util.LocatedBlock;

public class SetLocatedBlocks implements Operation {
   private final Extent extent;
   private final Iterable<LocatedBlock> blocks;

   public SetLocatedBlocks(Extent extent, Iterable<LocatedBlock> blocks) {
      this.extent = (Extent)Preconditions.checkNotNull(extent);
      this.blocks = (Iterable<LocatedBlock>)Preconditions.checkNotNull(blocks);
   }

   @Override
   public Operation resume(RunContext run) throws WorldEditException {
      for (LocatedBlock block : this.blocks) {
         this.extent.setBlock(block.getLocation(), block.getBlock());
      }

      return null;
   }

   @Override
   public void cancel() {
   }
}

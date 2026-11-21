package com.fastasyncworldedit.core.function.block;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

public class CombinedBlockCopy implements RegionFunction {
   protected final Extent source;
   protected final Extent destination;
   private final RegionFunction function;

   public CombinedBlockCopy(Extent source, Extent destination, RegionFunction combined) {
      this.source = source;
      this.destination = destination;
      this.function = combined;
   }

   @Override
   public boolean apply(BlockVector3 position) throws WorldEditException {
      BaseBlock block = this.source.getFullBlock(position);
      this.function.apply(position);
      return this.destination.setBlock(position, block);
   }
}

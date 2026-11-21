package com.fastasyncworldedit.core.queue;

import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import javax.annotation.Nullable;

public interface IChunk extends Trimable, IChunkGet, IChunkSet {
   default <V extends IChunk> void init(IQueueExtent<V> extent, int x, int z) {
   }

   @Override
   int getX();

   @Override
   int getZ();

   default BlockVector3 getChunkBlockCoord() {
      return BlockVector3.at(this.getX() << 4, this.getMinY(), this.getZ() << 4);
   }

   default IChunk getRoot() {
      return this;
   }

   void filterBlocks(Filter filter, ChunkFilterBlock block, @Nullable Region region, boolean full);

   default IChunk reset() {
      return this;
   }
}

package com.fastasyncworldedit.core.extent.filter.block;

import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.FilterBlockMask;
import com.fastasyncworldedit.core.queue.IBlocks;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.queue.implementation.Flood;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.regions.Region;

public abstract class ChunkFilterBlock extends AbstractExtentFilterBlock {
   public ChunkFilterBlock(Extent extent) {
      super(extent);
   }

   public abstract ChunkFilterBlock initChunk(int chunkX, int chunkZ);

   public abstract ChunkFilterBlock initLayer(IBlocks iget, IChunkSet iset, int layer);

   public abstract void flood(IChunkGet iget, IChunkSet iset, int layer, Flood flood, FilterBlockMask mask);

   public abstract void filter(Filter filter, int x, int y, int z);

   public abstract void filter(Filter filter, int minX, int minY, int minZ, int maxX, int maxY, int maxZ);

   public abstract void filter(Filter filter);

   public abstract void filter(Filter filter, int startY, int endY);

   public abstract void filter(Filter filter, Region region);

   public final synchronized IChunkSet filter(IChunk chunk, IChunkGet get, IChunkSet set, Filter filter) {
      this.initChunk(chunk.getX(), chunk.getZ());

      for (int layer = get.getMinSectionPosition(); layer <= get.getMaxSectionPosition(); layer++) {
         if (set.hasSection(layer)) {
            this.initLayer(get, set, layer);
            this.filter(filter);
         }
      }

      return set;
   }

   public final synchronized IChunkSet filter(IChunk chunk, IChunkGet get, IChunkSet set, Filter filter, Region region, boolean full) {
      if (region != null) {
         region.filter(chunk, filter, this, get, set, full);
      } else {
         for (int layer = get.getMinSectionPosition(); layer <= get.getMaxSectionPosition(); layer++) {
            if (full || get.hasSection(layer)) {
               this.initLayer(get, set, layer);
               this.filter(filter);
            }
         }
      }

      return set;
   }
}

package com.fastasyncworldedit.core.extent;

import com.fastasyncworldedit.core.limit.FaweLimit;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.regions.Region;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Future;

public class SingleRegionExtent extends FaweRegionExtent {
   private final Region region;

   public SingleRegionExtent(Extent extent, FaweLimit limit, Region region) {
      super(extent, limit);
      this.region = region;
   }

   @Override
   public boolean contains(int x, int y, int z) {
      return this.region.contains(x, y, z);
   }

   @Override
   public boolean contains(int x, int z) {
      return this.region.contains(x, z);
   }

   @Override
   public Collection<Region> getRegions() {
      return Collections.singletonList(this.region);
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      return this.region.processSet(chunk, get, set);
   }

   @Override
   public Future<?> postProcessSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      return this.region.postProcessSet(chunk, get, set);
   }

   @Override
   public void postProcess(IChunk chunk, IChunkGet get, IChunkSet set) {
      this.region.postProcess(chunk, get, set);
   }

   @Override
   public boolean processGet(int chunkX, int chunkZ) {
      return this.region.containsChunk(chunkX, chunkZ);
   }
}

package com.sk89q.worldedit.regions;

import com.fastasyncworldedit.core.configuration.Caption;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.fastasyncworldedit.core.util.MultiFuture;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

public class RegionIntersection extends AbstractRegion {
   private final List<Region> regions = new ArrayList<>();

   public RegionIntersection(List<Region> regions) {
      this(null, regions);
   }

   public RegionIntersection(Region... regions) {
      this(null, regions);
   }

   public RegionIntersection(World world, Collection<Region> regions) {
      super(world);
      Preconditions.checkNotNull(regions);
      Preconditions.checkArgument(!regions.isEmpty(), "empty region list is not supported");
      this.regions.addAll(regions);
   }

   public RegionIntersection(World world, Region... regions) {
      super(world);
      Preconditions.checkNotNull(regions);
      Preconditions.checkArgument(regions.length > 0, "empty region list is not supported");
      Collections.addAll(this.regions, regions);
   }

   @Override
   public BlockVector3 getMinimumPoint() {
      BlockVector3 minimum = this.regions.get(0).getMinimumPoint();

      for (int i = 1; i < this.regions.size(); i++) {
         minimum = this.regions.get(i).getMinimumPoint().getMinimum(minimum);
      }

      return minimum;
   }

   @Override
   public BlockVector3 getMaximumPoint() {
      BlockVector3 maximum = this.regions.get(0).getMaximumPoint();

      for (int i = 1; i < this.regions.size(); i++) {
         maximum = this.regions.get(i).getMaximumPoint().getMaximum(maximum);
      }

      return maximum;
   }

   @Override
   public void expand(BlockVector3... changes) throws RegionOperationException {
      Preconditions.checkNotNull(changes);
      throw new RegionOperationException(Caption.of("worldedit.selection.intersection.error.cannot-expand"));
   }

   @Override
   public void contract(BlockVector3... changes) throws RegionOperationException {
      Preconditions.checkNotNull(changes);
      throw new RegionOperationException(Caption.of("worldedit.selection.intersection.error.cannot-contract"));
   }

   @Override
   public boolean contains(BlockVector3 position) {
      Preconditions.checkNotNull(position);

      for (Region region : this.regions) {
         if (region.contains(position)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public Iterator<BlockVector3> iterator() {
      return Iterators.concat(Iterators.transform(this.regions.iterator(), r -> r.iterator()));
   }

   @Override
   public boolean containsEntireCuboid(int bx, int tx, int by, int ty, int bz, int tz) {
      for (Region region : this.regions) {
         if (region.containsEntireCuboid(bx, tx, by, ty, bz, tz)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      int bx = chunk.getX() << 4;
      int bz = chunk.getZ() << 4;
      int tx = bx + 15;
      int tz = bz + 15;
      List<Region> intersecting = new ArrayList<>(2);

      for (Region region : this.regions) {
         BlockVector3 regMin = region.getMinimumPoint();
         BlockVector3 regMax = region.getMaximumPoint();
         if (tx >= regMin.x() && bx <= regMax.x() && tz >= regMin.z() && bz <= regMax.z()) {
            intersecting.add(region);
         }
      }

      if (intersecting.isEmpty()) {
         return null;
      } else {
         return intersecting.size() == 1 ? intersecting.get(0).processSet(chunk, get, set) : super.processSet(chunk, get, set);
      }
   }

   @Override
   public Future<?> postProcessSet(final IChunk chunk, final IChunkGet get, final IChunkSet set) {
      ArrayList<Future<?>> futures = new ArrayList<>();

      for (Region region : this.regions) {
         futures.add(region.postProcessSet(chunk, get, set));
      }

      return new MultiFuture(futures);
   }

   @Override
   public IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set, boolean asBlacklist) {
      if (!asBlacklist) {
         return this.processSet(chunk, get, set);
      } else {
         int bx = chunk.getX() << 4;
         int bz = chunk.getZ() << 4;
         int tx = bx + 15;
         int tz = bz + 15;

         for (Region region : this.regions) {
            BlockVector3 regMin = region.getMinimumPoint();
            BlockVector3 regMax = region.getMaximumPoint();
            if (tx >= regMin.x() && bx <= regMax.x() && tz >= regMin.z() && bz <= regMax.z()) {
               set = region.processSet(chunk, get, set, true);
            }
         }

         return set;
      }
   }

   public List<Region> getRegions() {
      return this.regions;
   }

   @Override
   public Set<BlockVector2> getChunks() {
      Set<BlockVector2> set = null;

      for (Region region : this.regions) {
         if (set == null) {
            set = region.getChunks();
         } else {
            set = Sets.union(set, region.getChunks());
         }
      }

      return set;
   }

   @Override
   public Set<BlockVector3> getChunkCubes() {
      Set<BlockVector3> set = null;

      for (Region region : this.regions) {
         if (set == null) {
            set = region.getChunkCubes();
         } else {
            set = Sets.union(set, region.getChunkCubes());
         }
      }

      return set;
   }

   @Override
   public boolean containsChunk(int chunkX, int chunkZ) {
      for (Region region : this.regions) {
         if (region.containsChunk(chunkX, chunkZ)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean contains(int x, int z) {
      for (Region region : this.regions) {
         if (region.contains(x, z)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean contains(int x, int y, int z) {
      for (Region region : this.regions) {
         if (region.contains(x, y, z)) {
            return true;
         }
      }

      return false;
   }
}

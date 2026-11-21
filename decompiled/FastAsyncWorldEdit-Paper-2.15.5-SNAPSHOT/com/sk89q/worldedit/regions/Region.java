package com.sk89q.worldedit.regions;

import com.fastasyncworldedit.core.FaweCache;
import com.fastasyncworldedit.core.extent.SingleRegionExtent;
import com.fastasyncworldedit.core.extent.filter.block.ChunkFilterBlock;
import com.fastasyncworldedit.core.extent.processor.ProcessorScope;
import com.fastasyncworldedit.core.queue.Filter;
import com.fastasyncworldedit.core.queue.IBatchProcessor;
import com.fastasyncworldedit.core.queue.IChunk;
import com.fastasyncworldedit.core.queue.IChunkGet;
import com.fastasyncworldedit.core.queue.IChunkSet;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.World;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

public interface Region extends Iterable<BlockVector3>, Cloneable, IBatchProcessor {
   BlockVector3 getMinimumPoint();

   BlockVector3 getMaximumPoint();

   default BlockVector3 getDimensions() {
      return this.getMaximumPoint().subtract(this.getMinimumPoint()).add(1, 1, 1);
   }

   default CuboidRegion getBoundingBox() {
      return new CuboidRegion(this.getMinimumPoint(), this.getMaximumPoint());
   }

   default Vector3 getCenter() {
      return this.getMinimumPoint().add(this.getMaximumPoint()).toVector3().divide(2.0);
   }

   @Deprecated
   default int getArea() {
      return (int)this.getVolume();
   }

   @NonAbstractForCompatibility(
      delegateName = "getArea",
      delegateParams = {}
   )
   default long getVolume() {
      DeprecationUtil.checkDelegatingOverride(this.getClass());
      return this.getArea();
   }

   int getWidth();

   int getHeight();

   int getLength();

   void expand(BlockVector3... changes) throws RegionOperationException;

   void contract(BlockVector3... changes) throws RegionOperationException;

   void shift(BlockVector3 change) throws RegionOperationException;

   boolean contains(BlockVector3 position);

   Set<BlockVector2> getChunks();

   Set<BlockVector3> getChunkCubes();

   @Nullable
   World getWorld();

   void setWorld(@Nullable World world);

   Region clone();

   List<BlockVector2> polygonize(int maxPoints);

   default boolean contains(int x, int y, int z) {
      return this.contains(BlockVector3.at(x, y, z));
   }

   default boolean contains(int x, int z) {
      return this.contains(BlockVector3.at(x, 0, z));
   }

   default boolean isGlobal() {
      BlockVector3 pos1 = this.getMinimumPoint();
      BlockVector3 pos2 = this.getMaximumPoint();
      return pos1.x() == Integer.MIN_VALUE
         && pos1.z() == Integer.MIN_VALUE
         && pos2.x() == Integer.MAX_VALUE
         && pos2.z() == Integer.MAX_VALUE
         && pos1.y() <= WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY()
         && pos2.y() >= WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY();
   }

   default int getMinimumY() {
      return this.getMinimumPoint().y();
   }

   default int getMaximumY() {
      return this.getMaximumPoint().y();
   }

   default void filter(final IChunk chunk, final Filter filter, ChunkFilterBlock block, final IChunkGet get, final IChunkSet set, boolean full) {
      int minSection = Math.max(get.getMinSectionPosition(), this.getMinimumY() >> 4);
      int maxSection = Math.min(get.getMaxSectionPosition(), this.getMaximumY() >> 4);

      for (int layer = minSection; layer <= maxSection; layer++) {
         if (!full && !get.hasSection(layer)) {
            return;
         }

         block = block.initLayer(get, set, layer);
         block.filter(filter, this);
      }
   }

   default void filter(
      final IChunk chunk, final Filter filter, ChunkFilterBlock block, final IChunkGet get, final IChunkSet set, final int minY, final int maxY, boolean full
   ) {
      int minSection = minY >> 4;
      int maxSection = maxY >> 4;
      int yStart = minY & 15;
      int yEnd = maxY & 15;
      if (minSection == maxSection) {
         this.filter(chunk, filter, block, get, set, minSection, yStart, yEnd, full);
      } else {
         if (yStart != 0) {
            this.filter(chunk, filter, block, get, set, minSection, yStart, 15, full);
            minSection++;
         }

         if (yEnd != 15) {
            this.filter(chunk, filter, block, get, set, maxSection, 0, yEnd, full);
            maxSection--;
         }

         for (int layer = minSection; layer <= maxSection; layer++) {
            this.filter(chunk, filter, block, get, set, layer, full);
         }
      }
   }

   default void filter(final IChunk chunk, final Filter filter, ChunkFilterBlock block, final IChunkGet get, final IChunkSet set, int layer, boolean full) {
      if (full || get.hasSection(layer)) {
         block = block.initLayer(get, set, layer);
         block.filter(filter);
      }
   }

   default void filter(
      final IChunk chunk,
      final Filter filter,
      ChunkFilterBlock block,
      final IChunkGet get,
      final IChunkSet set,
      int layer,
      int minX,
      int minY,
      int minZ,
      int maxX,
      int maxY,
      int maxZ,
      boolean full
   ) {
      if (full || get.hasSection(layer)) {
         block = block.initLayer(get, set, layer);
         block.filter(filter, minX, minY, minZ, maxX, maxY, maxZ);
      }
   }

   default void filter(
      final IChunk chunk, final Filter filter, ChunkFilterBlock block, final IChunkGet get, final IChunkSet set, int layer, int yStart, int yEnd, boolean full
   ) {
      if (full || get.hasSection(layer)) {
         block = block.initLayer(get, set, layer);
         block.filter(filter, yStart, yEnd);
      }
   }

   default boolean containsEntireCuboid(int bx, int tx, int by, int ty, int bz, int tz) {
      return this.contains(bx, by, bz)
         && this.contains(bx, by, tz)
         && this.contains(tx, by, bz)
         && this.contains(tx, by, tz)
         && this.contains(bx, ty, bz)
         && this.contains(bx, ty, tz)
         && this.contains(tx, ty, bz)
         && this.contains(tx, ty, tz);
   }

   default boolean containsChunk(int chunkX, int chunkZ) {
      int bx = chunkX << 4;
      int bz = chunkZ << 4;
      int tx = bx + 15;
      int tz = bz + 15;
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      return tx >= min.x() && bx <= max.x() && tz >= min.z() && bz <= max.z();
   }

   @Override
   default IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set) {
      int bx = chunk.getX() << 4;
      int bz = chunk.getZ() << 4;
      int tx = bx + 15;
      int tz = bz + 15;
      BlockVector3 min = this.getMinimumPoint();
      BlockVector3 max = this.getMaximumPoint();
      if (tx >= min.x() && bx <= max.x() && tz >= min.z() && bz <= max.z()) {
         boolean processExtra = false;
         int minLayer = Math.max(this.getMinimumY(), chunk.getMinY()) >> 4;
         int maxLayer = Math.min(this.getMaximumY(), chunk.getMaxY()) >> 4;

         for (int layer = minLayer; layer <= maxLayer; layer++) {
            if (set.hasSection(layer)) {
               int by = layer << 4;
               int ty = by + 15;
               if (!this.containsEntireCuboid(bx, tx, by, ty, bz, tz)) {
                  processExtra = true;
                  char[] arr = set.loadIfPresent(layer);
                  if (arr != null) {
                     int y = 0;

                     for (int index = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                           for (int x = 0; x < 16; index++) {
                              if (arr[index] != 0 && !this.contains(bx + x, by + y, bz + z)) {
                                 arr[index] = 0;
                              }

                              x++;
                           }
                        }
                     }

                     set.setBlocks(layer, arr);
                  }
               }
            }
         }

         if (processExtra) {
            BlockVector3 chunkPos = chunk.getChunkBlockCoord().withY(0);
            this.trimNBT(set, this::contains, pos -> this.contains(pos.add(chunkPos)));
         }

         return set;
      } else {
         return null;
      }
   }

   default IChunkSet processSet(IChunk chunk, IChunkGet get, IChunkSet set, boolean asBlacklist) {
      if (!asBlacklist) {
         return this.processSet(chunk, get, set);
      } else {
         int bx = chunk.getX() << 4;
         int bz = chunk.getZ() << 4;
         int tx = bx + 15;
         int tz = bz + 15;
         BlockVector3 min = this.getMinimumPoint();
         BlockVector3 max = this.getMaximumPoint();
         if (tx >= min.x() && bx <= max.x() && tz >= min.z() && bz <= max.z()) {
            boolean processExtra = false;
            int minLayer = Math.max(this.getMinimumY(), chunk.getMinY()) >> 4;
            int maxLayer = Math.min(this.getMaximumY(), chunk.getMaxY()) >> 4;

            for (int layer = minLayer; layer <= maxLayer; layer++) {
               int by = layer << 4;
               int ty = by + 15;
               if (this.containsEntireCuboid(bx, tx, by, ty, bz, tz)) {
                  set.setBlocks(layer, FaweCache.INSTANCE.EMPTY_CHAR_4096);
                  processExtra = true;
               } else {
                  char[] arr = set.load(layer);
                  int y = 0;

                  for (int index = 0; y < 16; y++) {
                     for (int z = 0; z < 16; z++) {
                        for (int x = 0; x < 16; index++) {
                           if (arr[index] != 0 && this.contains(x, y, z)) {
                              arr[index] = 0;
                              processExtra = true;
                           }

                           x++;
                        }
                     }
                  }

                  if (processExtra) {
                     set.setBlocks(layer, arr);
                  }
               }
            }

            if (processExtra) {
               BlockVector3 chunkPos = chunk.getChunkBlockCoord().withY(0);
               this.trimNBT(set, bv3 -> !this.contains(bv3), bv3 -> !this.contains(bv3.add(chunkPos)));
            }

            return set;
         } else {
            return set;
         }
      }
   }

   @Override
   default Extent construct(Extent child) {
      return (Extent)(this.isGlobal() ? child : new SingleRegionExtent(child, null, this));
   }

   @Override
   default ProcessorScope getScope() {
      return ProcessorScope.REMOVING_BLOCKS;
   }
}

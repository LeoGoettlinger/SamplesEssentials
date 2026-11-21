package com.fastasyncworldedit.core.util.collection;

import com.fastasyncworldedit.core.math.BlockVectorSet;
import com.fastasyncworldedit.core.math.LocalBlockVectorSet;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import java.util.Set;

public interface BlockVector3Set extends Set<BlockVector3> {
   static BlockVector3Set getAppropriateVectorSet(Region region) {
      BlockVector3 max = region.getMaximumPoint();
      BlockVector3 min = region.getMinimumPoint();
      BlockVector3Set set = getAppropriateVectorSet(region.getDimensions());
      int offsetX = (int)Math.ceil((min.x() + max.x()) / 2.0);
      int offsetZ = (int)Math.ceil((min.z() + max.z()) / 2.0);
      int offsetY;
      if (region.getMinimumY() >= -128 && region.getMaximumY() <= 320) {
         offsetY = 128;
      } else {
         offsetY = (min.y() + max.y()) / 2;
      }

      set.setOffset(offsetX, offsetY, offsetZ);
      return set;
   }

   static BlockVector3Set getAppropriateVectorSet(BlockVector3 size) {
      return (BlockVector3Set)(size.x() <= 2048 && size.z() <= 2048 && size.y() <= 512 ? new LocalBlockVectorSet() : new BlockVectorSet());
   }

   boolean add(int x, int y, int z);

   boolean contains(int x, int y, int z);

   void setOffset(int x, int z);

   void setOffset(int x, int y, int z);

   boolean containsRadius(int x, int y, int z, int radius);

   BlockVector3Set copy();
}

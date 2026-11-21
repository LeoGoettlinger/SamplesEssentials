package com.sk89q.worldedit.regions.iterator;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class FlatRegionIterator implements Iterator<BlockVector2> {
   private final Region region;
   private final int y;
   private final int minX;
   private final int maxX;
   private final int maxZ;
   private int nextX;
   private int nextZ;

   public FlatRegionIterator(Region region) {
      Preconditions.checkNotNull(region);
      this.region = region;
      BlockVector3 min = region.getMinimumPoint();
      BlockVector3 max = region.getMaximumPoint();
      this.y = min.y();
      this.minX = min.x();
      this.nextX = this.minX;
      this.nextZ = min.z();
      this.maxX = max.x();
      this.maxZ = max.z();
      this.forward();
   }

   @Override
   public boolean hasNext() {
      return this.nextX != Integer.MIN_VALUE;
   }

   private void forward() {
      while (this.hasNext() && !this.region.contains(BlockVector3.at(this.nextX, this.y, this.nextZ))) {
         this.forwardOne();
      }
   }

   public BlockVector2 next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         BlockVector2 answer = BlockVector2.at(this.nextX, this.nextZ);
         this.forwardOne();
         this.forward();
         return answer;
      }
   }

   private void forwardOne() {
      if (++this.nextX > this.maxX) {
         this.nextX = this.minX;
         if (++this.nextZ > this.maxZ) {
            this.nextX = Integer.MIN_VALUE;
         }
      }
   }
}

package com.fastasyncworldedit.core.function.pattern;

import com.fastasyncworldedit.core.extent.ExtentHeightCacher;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.pattern.AbstractPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;

public abstract class AnglePattern extends AbstractPattern {
   public final double factor;
   public final Extent extent;
   public final int maxY;
   public final int minY;
   public final int distance;

   public AnglePattern(Extent extent, int distance) {
      this.extent = new ExtentHeightCacher(extent);
      this.maxY = extent.getMaxY();
      this.minY = extent.getMinY();
      this.distance = distance;
      this.factor = 1.0 / distance * (1.0 / this.maxY);
   }

   public <T extends BlockStateHolder<T>> int getSlope(T block, BlockVector3 vector, Extent extent) {
      int x = vector.x();
      int y = vector.y();
      int z = vector.z();
      if (!block.getBlockType().getMaterial().isMovementBlocker()) {
         return -1;
      } else {
         int slope = Math.abs(
               extent.getNearestSurfaceTerrainBlock(x + this.distance, z, y, this.minY, this.maxY)
                  - extent.getNearestSurfaceTerrainBlock(x - this.distance, z, y, this.minY, this.maxY)
            )
            * 7;
         slope += Math.abs(
               extent.getNearestSurfaceTerrainBlock(x, z + this.distance, y, this.minY, this.maxY)
                  - extent.getNearestSurfaceTerrainBlock(x, z - this.distance, y, this.minY, this.maxY)
            )
            * 7;
         slope += Math.abs(
               extent.getNearestSurfaceTerrainBlock(x + this.distance, z + this.distance, y, this.minY, this.maxY)
                  - extent.getNearestSurfaceTerrainBlock(x - this.distance, z - this.distance, y, this.minY, this.maxY)
            )
            * 5;
         return slope
            + Math.abs(
                  extent.getNearestSurfaceTerrainBlock(x - this.distance, z + this.distance, y, this.minY, this.maxY)
                     - extent.getNearestSurfaceTerrainBlock(x + this.distance, z - this.distance, y, this.minY, this.maxY)
               )
               * 5;
      }
   }

   @Override
   public abstract BaseBlock applyBlock(BlockVector3 position);

   @Override
   public abstract boolean apply(Extent extent, BlockVector3 setPosition, BlockVector3 getPosition);
}

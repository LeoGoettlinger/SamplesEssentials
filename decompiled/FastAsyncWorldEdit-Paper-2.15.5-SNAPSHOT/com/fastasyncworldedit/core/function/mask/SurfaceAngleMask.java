package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import java.util.ArrayList;

public class SurfaceAngleMask extends AbstractExtentMask {
   private final double min;
   private final double max;
   private final int size;

   public SurfaceAngleMask(Extent extent, double min, double max, int size) {
      super(extent);
      this.min = min;
      this.max = max;
      this.size = size;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      if (!vector.getBlock(this.getExtent()).isAir() && this.nextToAir(vector)) {
         double angle = 1.0 - this.getAverageAirDirection(vector.toVector3(), this.size).y();
         return angle >= this.min / 90.0 && angle <= this.max / 90.0;
      } else {
         return false;
      }
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      if (!vector.getBlock(this.getExtent()).isAir() && this.nextToAir(vector)) {
         double angle = 1.0 - this.getAverageAirDirection(vector.toVector3(), this.size).y();
         return angle >= this.min / 90.0 && angle <= this.max / 90.0;
      } else {
         return false;
      }
   }

   private Vector3 getAverageAirDirection(Vector3 currentLocation, int size) {
      ArrayList<Vector3> airDirections = new ArrayList<>();

      for (int i = -size; i <= size; i++) {
         for (int j = -size; j <= size; j++) {
            for (int k = -size; k <= size; k++) {
               Vector3 block = Vector3.at(currentLocation.x(), currentLocation.y(), currentLocation.z()).add(0.5, 0.5, 0.5).add(i, j, k);
               if (block.toBlockPoint().clampY(this.getExtent().getMinY(), this.getExtent().getMaxY()).getBlock(this.getExtent()).getMaterial().isAir()) {
                  airDirections.add(block.subtract(currentLocation.add(0.5, 0.5, 0.5)));
               }
            }
         }
      }

      double x = 0.0;
      double y = 0.0;
      double z = 0.0;

      for (Vector3 vector3 : airDirections) {
         x += vector3.x();
         y += vector3.y();
         z += vector3.z();
      }

      Vector3 averageAirDirection = Vector3.at(x / airDirections.size(), y / airDirections.size(), z / airDirections.size());
      return Double.isNaN(averageAirDirection.y()) ? Vector3.ZERO : averageAirDirection.normalize();
   }

   @Override
   public Mask copy() {
      return new SurfaceAngleMask(super.getExtent(), this.min, this.max, this.size);
   }

   private boolean nextToAir(BlockVector3 blockVector3) {
      if (this.getExtent().getBlock(blockVector3.add(1, 0, 0)).toBaseBlock().getMaterial().isAir()) {
         return true;
      } else if (this.getExtent().getBlock(blockVector3.add(-1, 0, 0)).toBaseBlock().getMaterial().isAir()) {
         return true;
      } else if (this.getExtent().getBlock(blockVector3.add(0, 1, 0)).toBaseBlock().getMaterial().isAir()) {
         return true;
      } else if (this.getExtent().getBlock(blockVector3.add(0, -1, 0)).toBaseBlock().getMaterial().isAir()) {
         return true;
      } else {
         return this.getExtent().getBlock(blockVector3.add(0, 0, 1)).toBaseBlock().getMaterial().isAir()
            ? true
            : this.getExtent().getBlock(blockVector3.add(0, 0, -1)).toBaseBlock().getMaterial().isAir();
      }
   }
}

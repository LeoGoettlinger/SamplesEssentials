package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;

public class ROCAngleMask extends AngleMask {
   public ROCAngleMask(Extent extent, double min, double max, boolean overlay, int distance) {
      super(extent, min, max, overlay, distance);
   }

   @Override
   protected boolean testSlope(Extent extent, int x, int y, int z) {
      this.lastY = y;
      int base = this.getHeight(extent, x, y, z);
      double slope = (this.getHeight(extent, x + this.distance, y, z) - base - (base - this.getHeight(extent, x - this.distance, y, z))) * ADJACENT_MOD;
      double tmp = (this.getHeight(extent, x, y, z + this.distance) - base - (base - this.getHeight(extent, x, y, z - this.distance))) * ADJACENT_MOD;
      if (Math.abs(tmp) > Math.abs(slope)) {
         slope = tmp;
      }

      tmp = (this.getHeight(extent, x + this.distance, y, z + this.distance) - base - (base - this.getHeight(extent, x - this.distance, y, z - this.distance)))
         * DIAGONAL_MOD;
      if (Math.abs(tmp) > Math.abs(slope)) {
         slope = tmp;
      }

      tmp = (this.getHeight(extent, x - this.distance, y, z + this.distance) - base - (base - this.getHeight(extent, x + this.distance, y, z - this.distance)))
         * DIAGONAL_MOD;
      if (Math.abs(tmp) > Math.abs(slope)) {
         slope = tmp;
      }

      return this.lastValue = slope >= this.min && slope <= this.max;
   }

   @Override
   public Mask copy() {
      return new ROCAngleMask(this.getExtent(), this.min, this.max, this.overlay, this.distance);
   }
}

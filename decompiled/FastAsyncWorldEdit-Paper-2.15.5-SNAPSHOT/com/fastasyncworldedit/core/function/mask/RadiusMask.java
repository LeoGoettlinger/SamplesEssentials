package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.function.mask.AbstractMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class RadiusMask extends AbstractMask implements ResettableMask {
   private final int minSqr;
   private final int maxSqr;
   private transient BlockVector3 pos;

   public RadiusMask(int min, int max) {
      this.minSqr = min * min;
      this.maxSqr = max * max;
   }

   private RadiusMask(Integer minSqr, Integer maxSqr) {
      this.minSqr = minSqr;
      this.maxSqr = maxSqr;
   }

   @Override
   public void reset() {
      this.pos = null;
   }

   @Override
   public boolean test(BlockVector3 to) {
      if (this.pos == null) {
         this.pos = to.toImmutable();
      }

      int dx = this.pos.x() - to.x();
      int d = dx * dx;
      if (d > this.maxSqr) {
         return false;
      } else {
         int dz = this.pos.z() - to.z();
         d += dz * dz;
         if (d > this.maxSqr) {
            return false;
         } else {
            int dy = this.pos.y() - to.y();
            d += dy * dy;
            return d >= this.minSqr && d <= this.maxSqr;
         }
      }
   }

   @Override
   public Mask copy() {
      return new RadiusMask(this.minSqr, Integer.valueOf(this.maxSqr));
   }
}

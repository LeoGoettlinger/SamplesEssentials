package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.function.mask.AbstractMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class PlaneMask extends AbstractMask implements ResettableMask {
   private transient int mode = -1;
   private transient int originX = Integer.MAX_VALUE;
   private transient int originY = Integer.MAX_VALUE;
   private transient int originZ = Integer.MAX_VALUE;

   @Override
   public boolean test(BlockVector3 vector) {
      switch (this.mode) {
         case -1:
            this.originX = vector.x();
            this.originY = vector.y();
            this.originZ = vector.z();
            this.mode = 0;
            return true;
         case 0:
         case 1:
         case 2:
         case 4:
            int original = this.mode;
            if (this.originX != vector.x()) {
               this.mode &= 1;
            }

            if (this.originY != vector.y()) {
               this.mode &= 2;
            }

            if (this.originZ != vector.z()) {
               this.mode &= 4;
            }

            if (Integer.bitCount(this.mode) >= 3) {
               this.mode = original;
               return false;
            }
         case 3:
      }

      if (this.originX != vector.x() && (this.mode & 1) == 0) {
         return false;
      } else {
         return this.originZ != vector.z() && (this.mode & 4) == 0 ? false : this.originY == vector.y() || (this.mode & 2) != 0;
      }
   }

   @Override
   public void reset() {
      this.mode = -1;
   }

   @Override
   public Mask copy() {
      return new PlaneMask();
   }
}

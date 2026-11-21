package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.function.mask.AbstractMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class WallMask extends AbstractMask {
   private final int min;
   private final int max;
   private final Mask mask;
   private final MutableBlockVector3 vector;

   public WallMask(Mask mask, int requiredMin, int requiredMax) {
      this.mask = mask;
      this.min = requiredMin;
      this.max = requiredMax;
      this.vector = new MutableBlockVector3();
   }

   @Override
   public boolean test(BlockVector3 bv) {
      this.vector.setComponents(bv);
      int count = 0;
      double x = this.vector.x();
      double y = this.vector.y();
      double z = this.vector.z();
      this.vector.mutX(x + 1.0);
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 8) {
            this.vector.mutX(x);
            return true;
         }
      }

      this.vector.mutX(x - 1.0);
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 8) {
            this.vector.mutX(x);
            return true;
         }
      }

      this.vector.mutX(x);
      this.vector.mutZ(z + 1.0);
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 8) {
            this.vector.mutZ(z);
            return true;
         }
      }

      this.vector.mutZ(z - 1.0);
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 8) {
            this.vector.mutZ(z);
            return true;
         }
      }

      this.vector.mutZ(z);
      return count >= this.min && count <= this.max;
   }

   @Override
   public Mask copy() {
      return new WallMask(this.mask.copy(), this.min, this.max);
   }
}

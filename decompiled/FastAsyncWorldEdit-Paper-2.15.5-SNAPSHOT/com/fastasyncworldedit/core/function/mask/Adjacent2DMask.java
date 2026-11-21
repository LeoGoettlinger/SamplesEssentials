package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.function.mask.AbstractMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class Adjacent2DMask extends AbstractMask {
   private final int min;
   private final int max;
   private final Mask mask;
   private final MutableBlockVector3 vector;

   public Adjacent2DMask(Mask mask, int requiredMin, int requiredMax) {
      this.mask = mask;
      this.min = requiredMin;
      this.max = requiredMax;
      this.vector = new MutableBlockVector3();
   }

   @Override
   public boolean test(BlockVector3 bv) {
      this.vector.setComponents(bv);
      double x = bv.x();
      double z = bv.z();
      this.vector.mutX(x + 1.0);
      int count = 0;
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 4) {
            return true;
         }
      }

      this.vector.mutX(x - 1.0);
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 4) {
            return true;
         }
      }

      this.vector.mutX(x);
      this.vector.mutZ(z + 1.0);
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 4) {
            return true;
         }
      }

      this.vector.mutZ(z - 1.0);
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 4) {
            return true;
         }
      }

      return count >= this.min && count <= this.max;
   }

   @Override
   public Mask copy() {
      return new Adjacent2DMask(this.mask.copy(), this.min, this.max);
   }
}

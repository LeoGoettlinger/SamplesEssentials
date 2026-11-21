package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.function.mask.AbstractMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class AdjacentMask extends AbstractMask {
   private final int min;
   private final int max;
   private final Mask mask;
   private final MutableBlockVector3 vector;

   public AdjacentMask(Mask mask, int requiredMin, int requiredMax) {
      this.mask = mask;
      this.min = requiredMin;
      this.max = requiredMax;
      this.vector = new MutableBlockVector3();
   }

   @Override
   public boolean test(BlockVector3 bv) {
      this.vector.setComponents(bv);
      double x = bv.x();
      double y = bv.y();
      double z = bv.z();
      this.vector.mutX(x + 1.0);
      int count = 0;
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
      this.vector.mutY(y + 1.0);
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 8) {
            this.vector.mutY(y);
            return true;
         }
      }

      this.vector.mutY(y - 1.0);
      if (this.mask.test(this.vector)) {
         if (++count == this.min && this.max >= 8) {
            this.vector.mutY(y);
            return true;
         }
      }

      this.vector.mutY(y);
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
      return new AdjacentMask(this.mask.copy(), this.min, this.max);
   }
}

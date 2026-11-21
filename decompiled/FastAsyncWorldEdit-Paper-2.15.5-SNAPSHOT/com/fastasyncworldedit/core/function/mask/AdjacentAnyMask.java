package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.function.mask.AbstractMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class AdjacentAnyMask extends AbstractMask implements ResettableMask {
   private final CachedMask mask;
   private final MutableBlockVector3 mutable;
   final int minY;
   final int maxY;

   public AdjacentAnyMask(Mask mask, int minY, int maxY) {
      this.mask = CachedMask.cache(mask);
      this.mutable = new MutableBlockVector3();
      this.minY = minY;
      this.maxY = maxY;
   }

   AdjacentAnyMask(CachedMask mask, int minY, int maxY) {
      this.mask = mask;
      this.mutable = new MutableBlockVector3();
      this.minY = minY;
      this.maxY = maxY;
   }

   @Override
   public void reset() {
      this.mutable.setComponents(0, 0, 0);
   }

   public CachedMask getParentMask() {
      return this.mask;
   }

   @Override
   public boolean test(BlockVector3 v) {
      return this.direction(v) != null;
   }

   public BlockVector3 direction(BlockVector3 v) {
      int x = v.x();
      int y = v.y();
      int z = v.z();
      if (this.mask.test(this.mutable.setComponents(x + 1, y, z))) {
         return this.mutable.setComponents(1, 0, 0);
      } else if (this.mask.test(this.mutable.setComponents(x - 1, y, z))) {
         return this.mutable.setComponents(-1, 0, 0);
      } else if (this.mask.test(this.mutable.setComponents(x, y, z + 1))) {
         return this.mutable.setComponents(0, 0, 1);
      } else if (this.mask.test(this.mutable.setComponents(x, y, z - 1))) {
         return this.mutable.setComponents(0, 0, -1);
      } else if (y < this.maxY && this.mask.test(this.mutable.setComponents(x, y + 1, z))) {
         return this.mutable.setComponents(0, 1, 0);
      } else {
         return y > this.minY && this.mask.test(this.mutable.setComponents(x, y - 1, z)) ? this.mutable.setComponents(0, -1, 0) : null;
      }
   }

   @Override
   public Mask copy() {
      return new AdjacentAnyMask((CachedMask)this.mask.copy(), this.minY, this.maxY);
   }
}

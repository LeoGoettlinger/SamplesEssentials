package com.fastasyncworldedit.core.function.mask;

import com.fastasyncworldedit.core.math.MutableBlockVector3;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AbstractExtentMask;
import com.sk89q.worldedit.function.mask.AbstractMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class AdjacentAny2DMask extends AbstractMask {
   private final Mask mask;
   private final MutableBlockVector3 mutable;

   public AdjacentAny2DMask(Mask mask) {
      this(mask, true);
   }

   public AdjacentAny2DMask(Mask mask, boolean cache) {
      this.mask = (Mask)(cache ? CachedMask.cache(mask) : mask);
      this.mutable = new MutableBlockVector3();
   }

   @Override
   public boolean test(BlockVector3 v) {
      int x = v.x();
      int y = v.y();
      int z = v.z();
      if (this.mask.test(this.mutable.setComponents(x + 1, y, z))) {
         return true;
      } else if (this.mask.test(this.mutable.setComponents(x - 1, y, z))) {
         return true;
      } else {
         return this.mask.test(this.mutable.setComponents(x, y, z + 1)) ? true : this.mask.test(this.mutable.setComponents(x, y, z - 1));
      }
   }

   public boolean test(Extent extent, BlockVector3 position) {
      if (this.mask instanceof AbstractExtentMask extentMask) {
         int var7 = position.x();
         int y = position.y();
         int z = position.z();
         if (extentMask.test(extent, this.mutable.setComponents(var7 + 1, y, z))) {
            return true;
         } else if (extentMask.test(extent, this.mutable.setComponents(var7 - 1, y, z))) {
            return true;
         } else {
            return extentMask.test(extent, this.mutable.setComponents(var7, y, z + 1))
               ? true
               : extentMask.test(extent, this.mutable.setComponents(var7, y, z - 1));
         }
      } else {
         throw new UnsupportedOperationException("Adjacency mask must inherit from AbstractExtentMask");
      }
   }

   @Override
   public Mask copy() {
      return new AdjacentAny2DMask(this.mask.copy(), false);
   }
}

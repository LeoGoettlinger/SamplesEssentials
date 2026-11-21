package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.math.BlockVector2;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class MaskUnion2D extends MaskIntersection2D {
   public MaskUnion2D(Collection<Mask2D> masks) {
      super(masks);
   }

   public MaskUnion2D(Mask2D... mask) {
      super(mask);
   }

   @Override
   public boolean test(BlockVector2 vector) {
      for (Mask2D mask : this.getMasks()) {
         if (mask.test(vector)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public Mask2D copy2D() {
      Set<Mask2D> masksCopy = this.masks.stream().map(Mask2D::copy2D).collect(Collectors.toSet());
      return new MaskUnion2D(masksCopy);
   }
}

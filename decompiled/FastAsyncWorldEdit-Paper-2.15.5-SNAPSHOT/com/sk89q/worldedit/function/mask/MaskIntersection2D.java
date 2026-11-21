package com.sk89q.worldedit.function.mask;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector2;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class MaskIntersection2D implements Mask2D {
   protected final Set<Mask2D> masks = new HashSet<>();

   public MaskIntersection2D(Collection<Mask2D> masks) {
      Preconditions.checkNotNull(masks);
      this.masks.addAll(masks);
   }

   public MaskIntersection2D(Mask2D... mask) {
      this(Arrays.asList((Mask2D[])Preconditions.checkNotNull(mask)));
   }

   public void add(Collection<Mask2D> masks) {
      Preconditions.checkNotNull(masks);
      this.masks.addAll(masks);
   }

   public void add(Mask2D... mask) {
      this.add(Arrays.asList((Mask2D[])Preconditions.checkNotNull(mask)));
   }

   public Collection<Mask2D> getMasks() {
      return this.masks;
   }

   @Override
   public boolean test(BlockVector2 vector) {
      if (this.masks.isEmpty()) {
         return false;
      } else {
         for (Mask2D mask : this.masks) {
            if (!mask.test(vector)) {
               return false;
            }
         }

         return true;
      }
   }

   @Override
   public Mask2D copy2D() {
      Set<Mask2D> masksCopy = this.masks.stream().map(Mask2D::copy2D).collect(Collectors.toSet());
      return new MaskIntersection2D(masksCopy);
   }
}

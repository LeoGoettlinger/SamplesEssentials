package com.sk89q.worldedit.function.mask;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.math.BlockVector3;
import javax.annotation.Nullable;

public class BoundedHeightMask extends AbstractMask {
   private final int minY;
   private final int maxY;

   public BoundedHeightMask(int minY, int maxY) {
      Preconditions.checkArgument(minY <= maxY, "minY <= maxY required. minY:" + minY + " and maxY:" + maxY + " were given.");
      this.minY = minY;
      this.maxY = maxY;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      return vector.y() >= this.minY && vector.y() <= this.maxY;
   }

   @Nullable
   @Override
   public Mask2D toMask2D() {
      return null;
   }

   @Override
   public Mask copy() {
      return this;
   }
}

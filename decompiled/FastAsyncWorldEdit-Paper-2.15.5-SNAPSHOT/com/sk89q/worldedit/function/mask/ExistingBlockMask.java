package com.sk89q.worldedit.function.mask;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import javax.annotation.Nullable;

public class ExistingBlockMask extends AbstractExtentMask {
   public ExistingBlockMask(Extent extent) {
      super(extent);
   }

   @Override
   public boolean test(BlockVector3 vector) {
      return !vector.getBlock(this.getExtent()).getBlockType().getMaterial().isAir();
   }

   @Override
   public boolean test(Extent extent, BlockVector3 vector) {
      return !vector.getBlock(extent).getBlockType().getMaterial().isAir();
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

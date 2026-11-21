package com.sk89q.worldedit.function;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.mask.Mask2D;
import com.sk89q.worldedit.math.BlockVector2;

public class FlatRegionMaskingFilter implements FlatRegionFunction {
   private final FlatRegionFunction function;
   private final Mask2D mask;

   public FlatRegionMaskingFilter(Mask2D mask, FlatRegionFunction function) {
      Preconditions.checkNotNull(function);
      Preconditions.checkNotNull(mask);
      this.mask = mask;
      this.function = function;
   }

   @Override
   public boolean apply(BlockVector2 position) throws WorldEditException {
      return this.mask.test(position) && this.function.apply(position);
   }
}

package com.sk89q.worldedit.function;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class RegionMaskingFilter implements RegionFunction {
   private final RegionFunction function;
   private final Mask mask;

   public RegionMaskingFilter(Mask mask, RegionFunction function) {
      Preconditions.checkNotNull(function);
      Preconditions.checkNotNull(mask);
      this.mask = mask;
      this.function = function;
   }

   @Deprecated(
      since = "2.12.3"
   )
   public RegionMaskingFilter(Extent extent, Mask mask, RegionFunction function) {
      this(mask, function);
   }

   @Override
   public boolean apply(BlockVector3 position) throws WorldEditException {
      return this.mask.test(position) && this.function.apply(position);
   }
}

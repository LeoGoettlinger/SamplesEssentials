package com.fastasyncworldedit.core.function;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class RegionMaskTestFunction implements RegionFunction {
   private final RegionFunction pass;
   private final RegionFunction fail;
   private final Mask mask;

   public RegionMaskTestFunction(Mask mask, RegionFunction success, RegionFunction failure) {
      Preconditions.checkNotNull(success);
      Preconditions.checkNotNull(failure);
      Preconditions.checkNotNull(mask);
      this.pass = success;
      this.fail = failure;
      this.mask = mask;
   }

   @Override
   public boolean apply(BlockVector3 position) throws WorldEditException {
      return this.mask.test(position) ? this.pass.apply(position) : this.fail.apply(position);
   }
}

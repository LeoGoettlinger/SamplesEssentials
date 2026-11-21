package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.Mask;

public class LiquidMask extends BlockMask {
   public LiquidMask(Extent extent) {
      super(extent);
      this.add(state -> state.getMaterial().isLiquid());
   }

   @Override
   public Mask copy() {
      return new LiquidMask(this.getExtent());
   }
}

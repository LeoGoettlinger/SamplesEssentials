package com.fastasyncworldedit.core.function.visitor;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.math.BlockVector3;

public class AboveVisitor extends RecursiveVisitor {
   private final int baseY;

   public AboveVisitor(Mask mask, RegionFunction function, int baseY, int depth, int minY, int maxY) {
      super(mask, function, depth, minY, maxY);
      Preconditions.checkNotNull(mask);
      this.baseY = baseY;
      this.setDirections(
         new BlockVector3[]{
            BlockVector3.UNIT_MINUS_X, BlockVector3.UNIT_MINUS_Y, BlockVector3.UNIT_MINUS_Z, BlockVector3.UNIT_X, BlockVector3.UNIT_Y, BlockVector3.UNIT_Z
         }
      );
   }

   @Override
   public boolean isVisitable(BlockVector3 from, BlockVector3 to) {
      return from.y() >= this.baseY && super.isVisitable(from, to);
   }
}

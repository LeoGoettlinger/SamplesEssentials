package com.fastasyncworldedit.core.function.visitor;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.math.BlockVector3;

public class DirectionalVisitor extends RecursiveVisitor {
   private final BlockVector3 origin;
   private final BlockVector3 dirVec;

   public DirectionalVisitor(Mask mask, RegionFunction function, BlockVector3 origin, BlockVector3 direction, int distance, int minY, int maxY) {
      super(mask, function, distance, minY, maxY);
      Preconditions.checkNotNull(mask);
      this.origin = origin;
      this.dirVec = direction;
      this.setDirections(
         new BlockVector3[]{
            BlockVector3.UNIT_MINUS_X, BlockVector3.UNIT_X, BlockVector3.UNIT_MINUS_Y, BlockVector3.UNIT_Y, BlockVector3.UNIT_MINUS_Z, BlockVector3.UNIT_Z
         }
      );
   }

   @Override
   public boolean isVisitable(final BlockVector3 from, final BlockVector3 to) {
      int dx = to.x() - from.x();
      int dz = to.z() - from.z();
      int dy = to.y() - from.y();
      if (dx != 0 && this.dirVec.x() != 0 && this.dirVec.x() != dx) {
         return false;
      } else if (dy != 0 && this.dirVec.y() != 0 && this.dirVec.y() != dy) {
         return false;
      } else {
         return dz != 0 && this.dirVec.z() != 0 && this.dirVec.z() != dz ? false : super.isVisitable(from, to);
      }
   }
}

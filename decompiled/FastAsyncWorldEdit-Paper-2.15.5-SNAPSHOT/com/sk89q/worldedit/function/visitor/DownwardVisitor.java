package com.sk89q.worldedit.function.visitor;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class DownwardVisitor extends RecursiveVisitor {
   private final int baseY;

   @Deprecated
   public DownwardVisitor(Mask mask, RegionFunction function, int baseY) {
      this(
         mask,
         function,
         baseY,
         Integer.MAX_VALUE,
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY(),
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY(),
         null
      );
   }

   public DownwardVisitor(Mask mask, RegionFunction function, int baseY, int depth, int minY, int maxY) {
      this(mask, function, baseY, depth, minY, maxY, null);
   }

   public DownwardVisitor(Mask mask, RegionFunction function, int baseY, int depth, int minY, int maxY, Extent extent) {
      super(mask, function, depth, minY, maxY, extent);
      Preconditions.checkNotNull(mask);
      this.baseY = baseY;
      this.setDirections(
         new BlockVector3[]{BlockVector3.UNIT_X, BlockVector3.UNIT_MINUS_X, BlockVector3.UNIT_Z, BlockVector3.UNIT_MINUS_Z, BlockVector3.UNIT_MINUS_Y}
      );
   }

   @Override
   protected boolean isVisitable(BlockVector3 from, BlockVector3 to) {
      int fromY = from.y();
      return (fromY == this.baseY || to.subtract(from).y() < 0) && super.isVisitable(from, to);
   }
}

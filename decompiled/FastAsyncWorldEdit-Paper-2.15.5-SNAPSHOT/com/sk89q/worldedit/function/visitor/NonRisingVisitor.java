package com.sk89q.worldedit.function.visitor;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class NonRisingVisitor extends RecursiveVisitor {
   @Deprecated
   public NonRisingVisitor(Mask mask, RegionFunction function) {
      this(
         mask,
         function,
         Integer.MAX_VALUE,
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY(),
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY(),
         null
      );
   }

   public NonRisingVisitor(Mask mask, RegionFunction function, int depth, int minY, int maxY) {
      this(mask, function, Integer.MAX_VALUE, minY, maxY, null);
   }

   public NonRisingVisitor(Mask mask, RegionFunction function, int depth, int minY, int maxY, Extent extent) {
      super(mask, function, depth, minY, maxY, extent);
      this.setDirections(
         new BlockVector3[]{BlockVector3.UNIT_X, BlockVector3.UNIT_MINUS_X, BlockVector3.UNIT_Z, BlockVector3.UNIT_MINUS_Z, BlockVector3.UNIT_MINUS_Y}
      );
   }
}

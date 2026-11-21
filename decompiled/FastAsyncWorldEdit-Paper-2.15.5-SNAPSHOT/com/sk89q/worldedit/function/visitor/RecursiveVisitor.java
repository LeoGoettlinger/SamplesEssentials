package com.sk89q.worldedit.function.visitor;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;

public class RecursiveVisitor extends BreadthFirstSearch {
   private final Mask mask;

   public RecursiveVisitor(Mask mask, RegionFunction function) {
      this(
         mask,
         function,
         Integer.MAX_VALUE,
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMinY(),
         WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.WORLD_EDITING).versionMaxY(),
         null
      );
   }

   public RecursiveVisitor(Mask mask, RegionFunction function, int maxDepth, int minY, int maxY) {
      this(mask, function, maxDepth, minY, maxY, null);
   }

   public RecursiveVisitor(Mask mask, RegionFunction function, int maxDepth, int minY, int maxY, Extent extent) {
      super(function, maxDepth, minY, maxY, extent);
      Preconditions.checkNotNull(mask);
      this.mask = mask;
   }

   @Override
   protected boolean isVisitable(BlockVector3 from, BlockVector3 to) {
      return this.mask.test(to);
   }
}

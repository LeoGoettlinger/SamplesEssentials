package com.sk89q.worldedit.function.pattern;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;

public class RepeatingExtentPattern extends AbstractExtentPattern {
   private final BlockVector3 size;
   private BlockVector3 origin;
   private BlockVector3 offset;

   public RepeatingExtentPattern(Extent extent, BlockVector3 origin, BlockVector3 offset) {
      super(extent);
      this.setOrigin(origin);
      this.setOffset(offset);
      this.size = extent.getMaximumPoint().subtract(extent.getMinimumPoint()).add(1, 1, 1);
   }

   public BlockVector3 getOffset() {
      return this.offset;
   }

   public void setOffset(BlockVector3 offset) {
      Preconditions.checkNotNull(offset);
      this.offset = offset;
   }

   public BlockVector3 getOrigin() {
      return this.origin;
   }

   public void setOrigin(BlockVector3 origin) {
      Preconditions.checkNotNull(origin);
      this.origin = origin;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      int x = Math.floorMod(position.x() + this.offset.x(), this.size.x()) + this.origin.x();
      int y = Math.floorMod(position.y() + this.offset.y(), this.size.y()) + this.origin.y();
      int z = Math.floorMod(position.z() + this.offset.z(), this.size.z()) + this.origin.z();
      return this.getExtent().getFullBlock(x, y, z);
   }
}

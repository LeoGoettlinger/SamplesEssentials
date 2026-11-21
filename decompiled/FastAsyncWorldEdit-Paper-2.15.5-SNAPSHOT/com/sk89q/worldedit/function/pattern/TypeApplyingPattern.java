package com.sk89q.worldedit.function.pattern;

import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;

public class TypeApplyingPattern extends AbstractExtentPattern {
   private final BlockState blockState;

   public TypeApplyingPattern(Extent extent, BlockState blockState) {
      super(extent);
      this.blockState = blockState;
   }

   @Override
   public BaseBlock applyBlock(BlockVector3 position) {
      BlockState oldBlock = this.getExtent().getBlock(position);
      BlockState newBlock = this.blockState.withProperties(oldBlock);
      return newBlock.toBaseBlock();
   }
}

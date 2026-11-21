package com.sk89q.worldedit.function.block;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.function.LayerFunction;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

public class Naturalizer implements LayerFunction {
   private final EditSession editSession;
   private final Mask mask;
   private int affected = 0;

   public Naturalizer(EditSession editSession) {
      Preconditions.checkNotNull(editSession);
      this.editSession = editSession;
      this.mask = new BlockTypeMask(editSession, BlockTypes.GRASS_BLOCK, BlockTypes.DIRT, BlockTypes.STONE);
   }

   public int getAffected() {
      return this.affected;
   }

   @Override
   public boolean isGround(BlockVector3 position) {
      return this.mask.test(position);
   }

   private BlockState getTargetBlock(int depth) {
      switch (depth) {
         case 0:
            return BlockTypes.GRASS_BLOCK.getDefaultState();
         case 1:
         case 2:
         case 3:
            return BlockTypes.DIRT.getDefaultState();
         default:
            return BlockTypes.STONE.getDefaultState();
      }
   }

   private boolean naturalize(BlockVector3 position, int depth) throws WorldEditException {
      return this.editSession.setBlock(position, this.getTargetBlock(depth));
   }

   @Override
   public boolean apply(BlockVector3 position, int depth) throws WorldEditException {
      if (this.mask.test(position) && this.naturalize(position, depth)) {
         this.affected++;
      }

      return true;
   }
}

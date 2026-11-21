package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

public class GravityBrush implements Brush {
   private final boolean fullHeight;

   public GravityBrush(boolean fullHeight) {
      this.fullHeight = fullHeight;
   }

   @Override
   public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
      double endY = position.y() + size;
      double startPerformY = Math.max((double)editSession.getMinY(), position.y() - size);
      double startCheckY = this.fullHeight ? editSession.getMinY() : startPerformY;

      for (double x = position.x() + size; x > position.x() - size; x--) {
         for (double z = position.z() + size; z > position.z() - size; z--) {
            double freeSpot = startCheckY;

            for (double y = startCheckY; y <= endY; y++) {
               BlockState block = editSession.getBlock((int)x, (int)y, (int)z);
               if (!block.getBlockType().getMaterial().isAir()) {
                  if (y != freeSpot) {
                     editSession.setBlock((int)x, (int)y, (int)z, BlockTypes.AIR.getDefaultState());
                     editSession.setBlock((int)x, (int)freeSpot, (int)z, block);
                  }

                  freeSpot = y + 1.0;
               }
            }
         }
      }
   }
}

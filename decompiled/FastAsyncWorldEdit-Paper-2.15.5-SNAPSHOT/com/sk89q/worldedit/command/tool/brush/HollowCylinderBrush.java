package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;

public class HollowCylinderBrush implements Brush {
   private final int height;
   private final double thickness;

   public HollowCylinderBrush(int height, double thickness) {
      this.height = height;
      this.thickness = thickness;
   }

   @Override
   public void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException {
      if (pattern == null) {
         pattern = BlockTypes.COBBLESTONE.getDefaultState();
      }

      editSession.makeCylinder(position, pattern, size, size, this.height, this.thickness, false);
   }
}

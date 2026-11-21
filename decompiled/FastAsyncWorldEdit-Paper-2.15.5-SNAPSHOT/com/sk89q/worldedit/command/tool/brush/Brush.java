package com.sk89q.worldedit.command.tool.brush;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

public interface Brush {
   void build(EditSession editSession, BlockVector3 position, Pattern pattern, double size) throws MaxChangedBlocksException;

   default boolean setsSynchronously() {
      return true;
   }
}

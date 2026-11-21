package com.sk89q.worldedit.function;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;

public interface LayerFunction {
   boolean isGround(BlockVector3 position);

   boolean apply(BlockVector3 position, int depth) throws WorldEditException;
}

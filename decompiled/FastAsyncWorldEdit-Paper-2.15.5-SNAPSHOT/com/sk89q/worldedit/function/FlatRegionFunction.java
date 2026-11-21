package com.sk89q.worldedit.function;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector2;

public interface FlatRegionFunction {
   boolean apply(BlockVector2 position) throws WorldEditException;
}

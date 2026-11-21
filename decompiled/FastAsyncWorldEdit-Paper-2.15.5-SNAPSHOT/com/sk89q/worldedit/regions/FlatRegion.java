package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.math.BlockVector2;

public interface FlatRegion extends Region {
   @Override
   int getMinimumY();

   @Override
   int getMaximumY();

   Iterable<BlockVector2> asFlatRegion();
}

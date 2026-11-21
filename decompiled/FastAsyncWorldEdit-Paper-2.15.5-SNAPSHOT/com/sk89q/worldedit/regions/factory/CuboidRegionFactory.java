package com.sk89q.worldedit.regions.factory;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

public class CuboidRegionFactory implements RegionFactory {
   @Override
   public Region createCenteredAt(BlockVector3 position, double size) {
      return CuboidRegion.fromCenter(position, (int)size);
   }
}

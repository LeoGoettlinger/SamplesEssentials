package com.sk89q.worldedit.regions.factory;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;

public interface RegionFactory {
   Region createCenteredAt(BlockVector3 position, double size);
}

package com.sk89q.worldedit.world.chunk;

import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import java.util.Collections;
import java.util.List;

public interface Chunk {
   BaseBlock getBlock(BlockVector3 position) throws DataException;

   default BiomeType getBiome(BlockVector3 position) throws DataException {
      return null;
   }

   default List<BaseEntity> getEntities() throws DataException {
      return Collections.emptyList();
   }
}

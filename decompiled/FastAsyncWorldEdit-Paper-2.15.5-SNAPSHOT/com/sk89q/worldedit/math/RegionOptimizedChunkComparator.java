package com.sk89q.worldedit.math;

import java.util.Comparator;

public class RegionOptimizedChunkComparator {
   private static final Comparator<BlockVector2> CHUNK_COMPARATOR = Comparator.<BlockVector2, BlockVector2>comparing(
         chunkPos -> chunkPos.shr(5), BlockVector2.COMPARING_GRID_ARRANGEMENT
      )
      .thenComparing(BlockVector2.COMPARING_GRID_ARRANGEMENT);
   public static final Comparator<BlockVector3> INSTANCE = Comparator.comparing(blockPos -> blockPos.toBlockVector2().shr(4), CHUNK_COMPARATOR);

   private RegionOptimizedChunkComparator() {
   }
}

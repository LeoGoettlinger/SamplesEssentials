package com.sk89q.worldedit.math;

import java.util.Comparator;

public class RegionOptimizedComparator {
   public static final Comparator<BlockVector3> INSTANCE = RegionOptimizedChunkComparator.INSTANCE.thenComparing(BlockVector3.sortByCoordsYzx());

   private RegionOptimizedComparator() {
   }
}

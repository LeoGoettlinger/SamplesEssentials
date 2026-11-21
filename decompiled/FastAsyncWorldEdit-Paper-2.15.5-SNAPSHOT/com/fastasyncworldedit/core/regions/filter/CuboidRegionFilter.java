package com.fastasyncworldedit.core.regions.filter;

import com.fastasyncworldedit.core.util.collection.LongHashSet;
import com.sk89q.worldedit.math.BlockVector2;

public abstract class CuboidRegionFilter implements RegionFilter {
   private final LongHashSet occupiedRegions = new LongHashSet();
   private final LongHashSet unoccupiedChunks = new LongHashSet();

   public abstract void calculateRegions();

   public void add(BlockVector2 pos1, BlockVector2 pos2) {
      int ccx1 = pos1.x() >> 9;
      int ccz1 = pos1.z() >> 9;
      int ccx2 = pos2.x() >> 9;
      int ccz2 = pos2.z() >> 9;

      for (int x = ccx1; x <= ccx2; x++) {
         for (int z = ccz1; z <= ccz2; z++) {
            if (!this.occupiedRegions.containsKey(x, z)) {
               this.occupiedRegions.add(x, z);
               int bcx = x << 5;
               int bcz = z << 5;
               int tcx = bcx + 32;
               int tcz = bcz + 32;

               for (int cz = bcz; cz < tcz; cz++) {
                  for (int cx = bcx; cx < tcx; cx++) {
                     this.unoccupiedChunks.add(cx, cz);
                  }
               }
            }
         }
      }

      int cx1 = pos1.x() >> 4;
      int cz1 = pos1.z() >> 4;
      int cx2 = pos2.x() >> 4;
      int cz2 = pos2.z() >> 4;

      for (int chunkZ = cz1; chunkZ <= cz2; chunkZ++) {
         for (int chunkX = cx1; chunkX <= cx2; chunkX++) {
            this.unoccupiedChunks.remove(chunkX, chunkZ);
         }
      }
   }

   public void clear() {
      this.occupiedRegions.popAll();
      this.unoccupiedChunks.popAll();
   }

   @Override
   public boolean containsRegion(int mcaX, int mcaZ) {
      return this.occupiedRegions.containsKey(mcaX, mcaZ);
   }

   @Override
   public boolean containsChunk(int chunkX, int chunkZ) {
      int mcaX = chunkX >> 5;
      int mcaZ = chunkZ >> 5;
      return this.occupiedRegions.containsKey(mcaX, mcaZ) && !this.unoccupiedChunks.containsKey(chunkX, chunkZ);
   }
}

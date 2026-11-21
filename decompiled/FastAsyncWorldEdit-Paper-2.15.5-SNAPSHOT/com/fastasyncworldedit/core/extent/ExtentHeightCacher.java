package com.fastasyncworldedit.core.extent;

import com.sk89q.worldedit.extent.Extent;
import java.util.Arrays;

public class ExtentHeightCacher extends PassthroughExtent {
   private transient int cacheBotX = Integer.MIN_VALUE;
   private transient int cacheBotZ = Integer.MIN_VALUE;
   private transient short[] cacheHeights;
   private transient int lastY;

   public ExtentHeightCacher(Extent extent) {
      super(extent);
   }

   public void reset() {
      this.cacheBotX = Integer.MIN_VALUE;
      this.cacheBotZ = Integer.MIN_VALUE;
      if (this.cacheHeights != null) {
         Arrays.fill(this.cacheHeights, (short)0);
      }
   }

   @Override
   public int getNearestSurfaceTerrainBlock(int x, int z, int y, int minY, int maxY) {
      int rx = x - this.cacheBotX + 16;
      int rz = z - this.cacheBotZ + 16;
      int index;
      if ((rx & 0xFF) == rx && (rz & 0xFF) == rz) {
         index = rx + (rz << 8);
      } else {
         this.cacheBotX = x - 16;
         this.cacheBotZ = z - 16;
         this.lastY = y;
         rx = x - this.cacheBotX + 16;
         rz = z - this.cacheBotZ + 16;
         index = rx + (rz << 8);
         if (this.cacheHeights == null) {
            this.cacheHeights = new short[65536];
            Arrays.fill(this.cacheHeights, (short)minY);
         } else {
            Arrays.fill(this.cacheHeights, (short)minY);
         }
      }

      int result = this.cacheHeights[index] & 255;
      if (result == minY) {
         this.cacheHeights[index] = (short)(result = this.lastY = super.getNearestSurfaceTerrainBlock(x, z, this.lastY, minY, maxY));
      }

      return result;
   }
}

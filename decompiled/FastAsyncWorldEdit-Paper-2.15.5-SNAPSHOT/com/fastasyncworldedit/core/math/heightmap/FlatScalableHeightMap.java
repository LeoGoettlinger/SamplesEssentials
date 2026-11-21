package com.fastasyncworldedit.core.math.heightmap;

public class FlatScalableHeightMap extends ScalableHeightMap {
   @Override
   public double getHeight(int x, int z) {
      int dx = Math.abs(x);
      int dz = Math.abs(z);
      int d2 = dx * dx + dz * dz;
      return d2 > this.size2 ? 0.0 : this.size;
   }
}

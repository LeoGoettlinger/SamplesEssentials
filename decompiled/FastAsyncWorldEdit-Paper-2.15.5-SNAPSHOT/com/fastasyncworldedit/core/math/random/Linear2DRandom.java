package com.fastasyncworldedit.core.math.random;

public class Linear2DRandom implements SimpleRandom {
   private final int xScale;
   private final int zScale;

   public Linear2DRandom(final int xScale, final int zScale) {
      this.xScale = xScale;
      this.zScale = zScale;
   }

   @Override
   public double nextDouble(final int x, final int y, final int z) {
      return this.nextDouble(x, y, z, 1.0);
   }

   @Override
   public double nextDouble(final int x, final int y, final int z, double bound) {
      double index = (Linear3DRandom.doubleDiv(x, this.xScale) + Linear3DRandom.doubleDiv(z, this.zScale)) % bound;
      if (index < 0.0) {
         index += bound;
      }

      return index;
   }

   @Override
   public int nextInt(final int x, final int y, final int z, final int bound) {
      int index = (Math.floorDiv(x, this.xScale) + Math.floorDiv(z, this.zScale)) % bound;
      if (index < 0) {
         index += bound;
      }

      return index;
   }
}

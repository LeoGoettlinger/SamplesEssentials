package com.fastasyncworldedit.core.math.random;

public class Linear3DRandom implements SimpleRandom {
   private final int xScale;
   private final int yScale;
   private final int zScale;

   public Linear3DRandom(final int xScale, final int yScale, final int zScale) {
      this.xScale = xScale;
      this.yScale = yScale;
      this.zScale = zScale;
   }

   @Override
   public double nextDouble(final int x, final int y, final int z) {
      return this.nextDouble(x, y, z, 1.0);
   }

   @Override
   public double nextDouble(final int x, final int y, final int z, double bound) {
      double index = (doubleDiv(x, this.xScale) + doubleDiv(y, this.yScale) + doubleDiv(z, this.zScale)) % bound;
      if (index < 0.0) {
         index += bound;
      }

      return index;
   }

   static double doubleDiv(double dividend, double divisor) {
      return Math.nextUp(dividend) / divisor;
   }

   @Override
   public int nextInt(final int x, final int y, final int z, final int bound) {
      int index = (Math.floorDiv(x, this.xScale) + Math.floorDiv(y, this.yScale) + Math.floorDiv(z, this.zScale)) % bound;
      if (index < 0) {
         index += bound;
      }

      return index;
   }
}

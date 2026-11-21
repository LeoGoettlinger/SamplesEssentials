package com.fastasyncworldedit.core.math.random;

public interface SimpleRandom {
   double nextDouble(int x, int y, int z);

   default double nextDouble(int x, int y, int z, double bound) {
      return this.nextDouble(x, y, z) * bound;
   }

   default int nextInt(int x, int y, int z, int bound) {
      double val = this.nextDouble(x, y, z, bound);
      return (int)val;
   }
}

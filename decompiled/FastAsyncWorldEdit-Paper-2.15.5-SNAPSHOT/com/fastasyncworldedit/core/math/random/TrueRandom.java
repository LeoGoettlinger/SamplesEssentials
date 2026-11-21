package com.fastasyncworldedit.core.math.random;

import java.util.SplittableRandom;

public class TrueRandom implements SimpleRandom {
   private final SplittableRandom random = new SplittableRandom();

   @Override
   public double nextDouble(int x, int y, int z) {
      return this.random.nextDouble();
   }

   @Override
   public int nextInt(int x, int y, int z, int len) {
      return this.random.nextInt(len);
   }
}

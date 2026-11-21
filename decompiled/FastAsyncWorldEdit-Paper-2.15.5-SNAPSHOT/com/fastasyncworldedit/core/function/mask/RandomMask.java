package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.function.mask.AbstractMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import java.util.SplittableRandom;

public class RandomMask extends AbstractMask implements ResettableMask {
   private transient SplittableRandom random = new SplittableRandom();
   private final double threshold;

   public RandomMask(double threshold) {
      this.threshold = (threshold - 0.5) * 2.147483647E9;
   }

   @Override
   public boolean test(BlockVector3 vector) {
      return this.random.nextInt() <= this.threshold;
   }

   @Override
   public void reset() {
      this.random = new SplittableRandom();
   }

   @Override
   public Mask copy() {
      return new RandomMask(this.threshold);
   }
}

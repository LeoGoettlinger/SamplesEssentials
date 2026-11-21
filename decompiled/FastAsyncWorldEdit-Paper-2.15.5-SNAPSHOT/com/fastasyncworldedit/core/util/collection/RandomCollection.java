package com.fastasyncworldedit.core.util.collection;

import com.fastasyncworldedit.core.math.random.SimpleRandom;
import com.google.common.base.Preconditions;
import java.util.Map;

public abstract class RandomCollection<T> {
   private SimpleRandom random;

   protected RandomCollection(SimpleRandom random) {
      this.random = random;
   }

   public static <T> RandomCollection<T> of(Map<T, Double> weights, SimpleRandom random) {
      Preconditions.checkNotNull(random);
      return FastRandomCollection.create(weights, random).orElseGet(() -> new SimpleRandomCollection<>(weights, random));
   }

   public void setRandom(SimpleRandom random) {
      Preconditions.checkNotNull(random);
      this.random = random;
   }

   public SimpleRandom getRandom() {
      return this.random;
   }

   public abstract T next(int x, int y, int z);
}

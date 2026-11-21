package com.fastasyncworldedit.core.util.collection;

import com.fastasyncworldedit.core.math.random.SimpleRandom;
import com.fastasyncworldedit.core.util.MathMan;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

public class FastRandomCollection<T> extends RandomCollection<T> {
   private final T[] values;

   private FastRandomCollection(T[] values, SimpleRandom random) {
      super(random);
      this.values = values;
   }

   public static <T> Optional<RandomCollection<T>> create(Map<T, Double> weights, SimpleRandom random) {
      int max = 0;
      int[] counts = new int[weights.size()];
      Double[] weightDoubles = weights.values().toArray(new Double[0]);

      for (int i = 0; i < weightDoubles.length; i++) {
         int weight = (int)(weightDoubles[i] * 100.0);
         counts[i] = weight;
         if (weight != weightDoubles[i] * 100.0) {
            return Optional.empty();
         }

         if (weight > max) {
            max = weight;
         }
      }

      int gcd = MathMan.gcd(counts);
      if (max / gcd > 100000) {
         return Optional.empty();
      } else {
         ArrayList<T> parsed = new ArrayList<>();

         for (Entry<T, Double> entry : weights.entrySet()) {
            int num = (int)(100.0 * entry.getValue());

            for (int j = 0; j < num / gcd; j++) {
               parsed.add(entry.getKey());
            }
         }

         T[] values = (T[])parsed.toArray();
         FastRandomCollection<T> fastRandomCollection = new FastRandomCollection<>(values, random);
         return Optional.of(fastRandomCollection);
      }
   }

   @Override
   public T next(int x, int y, int z) {
      return this.values[this.getRandom().nextInt(x, y, z, this.values.length)];
   }
}

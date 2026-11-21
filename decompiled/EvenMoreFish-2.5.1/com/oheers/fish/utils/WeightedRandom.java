package com.oheers.fish.utils;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleFunction;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class WeightedRandom {
   private WeightedRandom() {
      throw new UnsupportedOperationException();
   }

   public static <T> T pick(List<T> elements, ToDoubleFunction<T> weightFunction, Random random) {
      return pick(elements, weightFunction, 1.0, Collections.emptySet(), random);
   }

   @Nullable
   public static <T> T pick(@NonNull List<T> elements, ToDoubleFunction<T> weightFunction, double boostRate, Set<T> boosted, Random random) {
      if (elements.isEmpty()) {
         return null;
      } else {
         Random rng = (Random)(random != null ? random : ThreadLocalRandom.current());
         double totalWeight = calcTotalWeight(elements, weightFunction, boostRate, boosted);
         if (totalWeight <= 0.0) {
            return elements.get(rng.nextInt(elements.size()));
         } else {
            double r = rng.nextDouble() * totalWeight;

            for (T element : elements) {
               double weight = getEffectiveWeight(element, weightFunction, boostRate, boosted);
               r -= weight;
               if (r <= 0.0) {
                  return element;
               }
            }

            return elements.getLast();
         }
      }
   }

   private static <T> double calcTotalWeight(@NonNull List<T> elements, ToDoubleFunction<T> weightFunction, double boostRate, Set<T> boosted) {
      double total = 0.0;

      for (T element : elements) {
         total += getEffectiveWeight(element, weightFunction, boostRate, boosted);
      }

      return total;
   }

   private static <T> double getEffectiveWeight(T element, ToDoubleFunction<T> weightFunction, double boostRate, Set<? extends T> boosted) {
      double baseWeight = weightFunction.applyAsDouble(element);
      if (baseWeight <= 0.0) {
         return 0.0;
      } else {
         return boostRate != -1.0 && boosted.contains(element) ? baseWeight * boostRate : baseWeight;
      }
   }
}

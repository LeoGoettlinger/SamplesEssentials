package com.magmaguy.elitemobs.utils;

import com.magmaguy.elitemobs.items.customitems.CustomItem;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

public class WeightedProbability {
   public static String pickWeighedProbability(HashMap<String, Double> weighedValues) {
      return pick(weighedValues);
   }

   public static CustomItem pickWeighedProbabilityFromCustomItems(HashMap<CustomItem, Double> weighedValues) {
      return pick(weighedValues);
   }

   public static <T> T pick(Map<T, Double> weights) {
      double scale = 0.0;

      for (double weight : weights.values()) {
         if (Double.isFinite(weight) && weight > scale) {
            scale = weight;
         }
      }

      if (scale == 0.0) {
         return null;
      } else {
         double total = 0.0;

         for (double weightx : weights.values()) {
            if (Double.isFinite(weightx) && weightx > 0.0) {
               total += weightx / scale;
            }
         }

         double selected = ThreadLocalRandom.current().nextDouble(total);
         T last = null;

         for (Entry<T, Double> entry : weights.entrySet()) {
            double weightxx = entry.getValue();
            if (Double.isFinite(weightxx) && !(weightxx <= 0.0)) {
               last = entry.getKey();
               selected -= weightxx / scale;
               if (selected < 0.0) {
                  return last;
               }
            }
         }

         return last;
      }
   }
}

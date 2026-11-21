package org.lushplugins.lushrewards.libraries.lushlib.utils;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomCollection<E> {
   private static final Random random = new Random();
   private final NavigableMap<Double, E> map = new TreeMap<>();
   private double total = 0.0;

   public RandomCollection<E> add(E result, double weight) {
      if (weight <= 0.0) {
         return this;
      } else {
         this.total += weight;
         this.map.put(this.total, result);
         return this;
      }
   }

   public E next() {
      double value = random.nextDouble() * this.total;
      return this.map.higherEntry(value).getValue();
   }

   public void clear() {
      this.map.clear();
      this.total = 0.0;
   }

   public boolean isEmpty() {
      return this.map.isEmpty();
   }

   public NavigableMap<Double, E> getMap() {
      return this.map;
   }

   public double getTotalWeight() {
      return this.total;
   }
}

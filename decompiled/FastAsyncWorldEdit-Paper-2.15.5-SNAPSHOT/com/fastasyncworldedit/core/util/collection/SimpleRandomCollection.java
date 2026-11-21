package com.fastasyncworldedit.core.util.collection;

import com.fastasyncworldedit.core.math.random.SimpleRandom;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class SimpleRandomCollection<E> extends RandomCollection<E> {
   private final NavigableMap<Double, E> map = new TreeMap<>();
   private double total = 0.0;

   public SimpleRandomCollection(Map<E, Double> weights, SimpleRandom random) {
      super(random);

      for (Entry<E, Double> entry : weights.entrySet()) {
         this.add(entry.getValue(), entry.getKey());
      }
   }

   public void add(double weight, E result) {
      if (!(weight <= 0.0)) {
         this.total += weight;
         this.map.put(this.total, result);
      }
   }

   @Override
   public E next(int x, int y, int z) {
      return this.map.ceilingEntry(this.getRandom().nextDouble(x, y, z, this.total)).getValue();
   }
}

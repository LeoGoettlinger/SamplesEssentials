package com.viaversion.viaversion.libs.fastutil.ints;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface IntComparator extends Comparator<Integer> {
   int compare(int var1, int var2);

   default IntComparator reversed() {
      return IntComparators.oppositeComparator(this);
   }

   @Deprecated
   default int compare(Integer ok1, Integer ok2) {
      return this.compare(ok1.intValue(), ok2.intValue());
   }

   default IntComparator thenComparing(IntComparator second) {
      return (IntComparator)((Serializable)((k1, k2) -> {
         int comp = this.compare(k1, k2);
         return comp == 0 ? second.compare(k1, k2) : comp;
      }));
   }

   @Override
   default Comparator<Integer> thenComparing(Comparator<? super Integer> second) {
      return (Comparator<Integer>)(second instanceof IntComparator ? this.thenComparing((IntComparator)second) : Comparator.super.thenComparing(second));
   }

   static <U extends Comparable<? super U>> IntComparator comparing(Int2ObjectFunction<? extends U> keyExtractor) {
      return (IntComparator)((Serializable)((k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2))));
   }

   static <U extends Comparable<? super U>> IntComparator comparing(Int2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
      Objects.requireNonNull(keyExtractor);
      return (IntComparator)((Serializable)((k1, k2) -> keyComparator.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static IntComparator comparingInt(Int2IntFunction keyExtractor) {
      return (IntComparator)((Serializable)((k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static IntComparator comparingLong(Int2LongFunction keyExtractor) {
      return (IntComparator)((Serializable)((k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static IntComparator comparingDouble(Int2DoubleFunction keyExtractor) {
      return (IntComparator)((Serializable)((k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }
}

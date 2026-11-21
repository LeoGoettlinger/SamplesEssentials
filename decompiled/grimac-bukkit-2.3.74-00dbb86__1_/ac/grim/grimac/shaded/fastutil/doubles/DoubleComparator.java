package ac.grim.grimac.shaded.fastutil.doubles;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface DoubleComparator extends Comparator<Double> {
   int compare(double var1, double var3);

   default DoubleComparator reversed() {
      return DoubleComparators.oppositeComparator(this);
   }

   @Deprecated
   default int compare(Double ok1, Double ok2) {
      return this.compare(ok1.doubleValue(), ok2.doubleValue());
   }

   default DoubleComparator thenComparing(DoubleComparator second) {
      return (DoubleComparator)((Serializable)((k1, k2) -> {
         int comp = this.compare(k1, k2);
         return comp == 0 ? second.compare(k1, k2) : comp;
      }));
   }

   @Override
   default Comparator<Double> thenComparing(Comparator<? super Double> second) {
      return (Comparator<Double>)(second instanceof DoubleComparator ? this.thenComparing((DoubleComparator)second) : Comparator.super.thenComparing(second));
   }

   static <U extends Comparable<? super U>> DoubleComparator comparing(Double2ObjectFunction<? extends U> keyExtractor) {
      return (DoubleComparator)((Serializable)((k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2))));
   }

   static <U extends Comparable<? super U>> DoubleComparator comparing(Double2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
      Objects.requireNonNull(keyExtractor);
      return (DoubleComparator)((Serializable)((k1, k2) -> keyComparator.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static DoubleComparator comparingInt(Double2IntFunction keyExtractor) {
      return (DoubleComparator)((Serializable)((k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static DoubleComparator comparingLong(Double2LongFunction keyExtractor) {
      return (DoubleComparator)((Serializable)((k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static DoubleComparator comparingDouble(Double2DoubleFunction keyExtractor) {
      return (DoubleComparator)((Serializable)((k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }
}

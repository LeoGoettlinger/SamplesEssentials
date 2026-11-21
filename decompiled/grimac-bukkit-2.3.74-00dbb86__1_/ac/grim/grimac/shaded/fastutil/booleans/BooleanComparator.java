package ac.grim.grimac.shaded.fastutil.booleans;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface BooleanComparator extends Comparator<Boolean> {
   int compare(boolean var1, boolean var2);

   default BooleanComparator reversed() {
      return BooleanComparators.oppositeComparator(this);
   }

   @Deprecated
   default int compare(Boolean ok1, Boolean ok2) {
      return this.compare(ok1.booleanValue(), ok2.booleanValue());
   }

   default BooleanComparator thenComparing(BooleanComparator second) {
      return (BooleanComparator)((Serializable)((k1, k2) -> {
         int comp = this.compare(k1, k2);
         return comp == 0 ? second.compare(k1, k2) : comp;
      }));
   }

   @Override
   default Comparator<Boolean> thenComparing(Comparator<? super Boolean> second) {
      return (Comparator<Boolean>)(second instanceof BooleanComparator ? this.thenComparing((BooleanComparator)second) : Comparator.super.thenComparing(second));
   }

   static <U extends Comparable<? super U>> BooleanComparator comparing(Boolean2ObjectFunction<? extends U> keyExtractor) {
      return (BooleanComparator)((Serializable)((k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2))));
   }

   static <U extends Comparable<? super U>> BooleanComparator comparing(Boolean2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
      Objects.requireNonNull(keyExtractor);
      return (BooleanComparator)((Serializable)((k1, k2) -> keyComparator.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static BooleanComparator comparingInt(Boolean2IntFunction keyExtractor) {
      return (BooleanComparator)((Serializable)((k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static BooleanComparator comparingLong(Boolean2LongFunction keyExtractor) {
      return (BooleanComparator)((Serializable)((k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static BooleanComparator comparingDouble(Boolean2DoubleFunction keyExtractor) {
      return (BooleanComparator)((Serializable)((k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }
}

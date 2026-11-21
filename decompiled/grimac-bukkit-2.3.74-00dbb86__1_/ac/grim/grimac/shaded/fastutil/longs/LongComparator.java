package ac.grim.grimac.shaded.fastutil.longs;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface LongComparator extends Comparator<Long> {
   int compare(long var1, long var3);

   default LongComparator reversed() {
      return LongComparators.oppositeComparator(this);
   }

   @Deprecated
   default int compare(Long ok1, Long ok2) {
      return this.compare(ok1.longValue(), ok2.longValue());
   }

   default LongComparator thenComparing(LongComparator second) {
      return (LongComparator)((Serializable)((k1, k2) -> {
         int comp = this.compare(k1, k2);
         return comp == 0 ? second.compare(k1, k2) : comp;
      }));
   }

   @Override
   default Comparator<Long> thenComparing(Comparator<? super Long> second) {
      return (Comparator<Long>)(second instanceof LongComparator ? this.thenComparing((LongComparator)second) : Comparator.super.thenComparing(second));
   }

   static <U extends Comparable<? super U>> LongComparator comparing(Long2ObjectFunction<? extends U> keyExtractor) {
      return (LongComparator)((Serializable)((k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2))));
   }

   static <U extends Comparable<? super U>> LongComparator comparing(Long2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
      Objects.requireNonNull(keyExtractor);
      return (LongComparator)((Serializable)((k1, k2) -> keyComparator.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static LongComparator comparingInt(Long2IntFunction keyExtractor) {
      return (LongComparator)((Serializable)((k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static LongComparator comparingLong(Long2LongFunction keyExtractor) {
      return (LongComparator)((Serializable)((k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static LongComparator comparingDouble(Long2DoubleFunction keyExtractor) {
      return (LongComparator)((Serializable)((k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }
}

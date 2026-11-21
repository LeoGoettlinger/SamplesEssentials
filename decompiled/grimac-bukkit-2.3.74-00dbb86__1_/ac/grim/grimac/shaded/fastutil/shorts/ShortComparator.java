package ac.grim.grimac.shaded.fastutil.shorts;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface ShortComparator extends Comparator<Short> {
   int compare(short var1, short var2);

   default ShortComparator reversed() {
      return ShortComparators.oppositeComparator(this);
   }

   @Deprecated
   default int compare(Short ok1, Short ok2) {
      return this.compare(ok1.shortValue(), ok2.shortValue());
   }

   default ShortComparator thenComparing(ShortComparator second) {
      return (ShortComparator)((Serializable)((k1, k2) -> {
         int comp = this.compare(k1, k2);
         return comp == 0 ? second.compare(k1, k2) : comp;
      }));
   }

   @Override
   default Comparator<Short> thenComparing(Comparator<? super Short> second) {
      return (Comparator<Short>)(second instanceof ShortComparator ? this.thenComparing((ShortComparator)second) : Comparator.super.thenComparing(second));
   }

   static <U extends Comparable<? super U>> ShortComparator comparing(Short2ObjectFunction<? extends U> keyExtractor) {
      return (ShortComparator)((Serializable)((k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2))));
   }

   static <U extends Comparable<? super U>> ShortComparator comparing(Short2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
      Objects.requireNonNull(keyExtractor);
      return (ShortComparator)((Serializable)((k1, k2) -> keyComparator.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static ShortComparator comparingInt(Short2IntFunction keyExtractor) {
      return (ShortComparator)((Serializable)((k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static ShortComparator comparingLong(Short2LongFunction keyExtractor) {
      return (ShortComparator)((Serializable)((k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static ShortComparator comparingDouble(Short2DoubleFunction keyExtractor) {
      return (ShortComparator)((Serializable)((k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }
}

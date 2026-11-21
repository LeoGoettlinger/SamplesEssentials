package ac.grim.grimac.shaded.fastutil.floats;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface FloatComparator extends Comparator<Float> {
   int compare(float var1, float var2);

   default FloatComparator reversed() {
      return FloatComparators.oppositeComparator(this);
   }

   @Deprecated
   default int compare(Float ok1, Float ok2) {
      return this.compare(ok1.floatValue(), ok2.floatValue());
   }

   default FloatComparator thenComparing(FloatComparator second) {
      return (FloatComparator)((Serializable)((k1, k2) -> {
         int comp = this.compare(k1, k2);
         return comp == 0 ? second.compare(k1, k2) : comp;
      }));
   }

   @Override
   default Comparator<Float> thenComparing(Comparator<? super Float> second) {
      return (Comparator<Float>)(second instanceof FloatComparator ? this.thenComparing((FloatComparator)second) : Comparator.super.thenComparing(second));
   }

   static <U extends Comparable<? super U>> FloatComparator comparing(Float2ObjectFunction<? extends U> keyExtractor) {
      return (FloatComparator)((Serializable)((k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2))));
   }

   static <U extends Comparable<? super U>> FloatComparator comparing(Float2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
      Objects.requireNonNull(keyExtractor);
      return (FloatComparator)((Serializable)((k1, k2) -> keyComparator.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static FloatComparator comparingInt(Float2IntFunction keyExtractor) {
      return (FloatComparator)((Serializable)((k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static FloatComparator comparingLong(Float2LongFunction keyExtractor) {
      return (FloatComparator)((Serializable)((k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static FloatComparator comparingDouble(Float2DoubleFunction keyExtractor) {
      return (FloatComparator)((Serializable)((k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }
}

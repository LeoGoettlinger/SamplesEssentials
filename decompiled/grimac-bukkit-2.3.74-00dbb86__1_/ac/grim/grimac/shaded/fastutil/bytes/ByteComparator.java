package ac.grim.grimac.shaded.fastutil.bytes;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface ByteComparator extends Comparator<Byte> {
   int compare(byte var1, byte var2);

   default ByteComparator reversed() {
      return ByteComparators.oppositeComparator(this);
   }

   @Deprecated
   default int compare(Byte ok1, Byte ok2) {
      return this.compare(ok1.byteValue(), ok2.byteValue());
   }

   default ByteComparator thenComparing(ByteComparator second) {
      return (ByteComparator)((Serializable)((k1, k2) -> {
         int comp = this.compare(k1, k2);
         return comp == 0 ? second.compare(k1, k2) : comp;
      }));
   }

   @Override
   default Comparator<Byte> thenComparing(Comparator<? super Byte> second) {
      return (Comparator<Byte>)(second instanceof ByteComparator ? this.thenComparing((ByteComparator)second) : Comparator.super.thenComparing(second));
   }

   static <U extends Comparable<? super U>> ByteComparator comparing(Byte2ObjectFunction<? extends U> keyExtractor) {
      return (ByteComparator)((Serializable)((k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2))));
   }

   static <U extends Comparable<? super U>> ByteComparator comparing(Byte2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
      Objects.requireNonNull(keyExtractor);
      return (ByteComparator)((Serializable)((k1, k2) -> keyComparator.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static ByteComparator comparingInt(Byte2IntFunction keyExtractor) {
      return (ByteComparator)((Serializable)((k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static ByteComparator comparingLong(Byte2LongFunction keyExtractor) {
      return (ByteComparator)((Serializable)((k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static ByteComparator comparingDouble(Byte2DoubleFunction keyExtractor) {
      return (ByteComparator)((Serializable)((k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }
}

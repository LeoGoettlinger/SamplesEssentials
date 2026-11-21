package ac.grim.grimac.shaded.fastutil.chars;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;

@FunctionalInterface
public interface CharComparator extends Comparator<Character> {
   int compare(char var1, char var2);

   default CharComparator reversed() {
      return CharComparators.oppositeComparator(this);
   }

   @Deprecated
   default int compare(Character ok1, Character ok2) {
      return this.compare(ok1.charValue(), ok2.charValue());
   }

   default CharComparator thenComparing(CharComparator second) {
      return (CharComparator)((Serializable)((k1, k2) -> {
         int comp = this.compare(k1, k2);
         return comp == 0 ? second.compare(k1, k2) : comp;
      }));
   }

   @Override
   default Comparator<Character> thenComparing(Comparator<? super Character> second) {
      return (Comparator<Character>)(second instanceof CharComparator ? this.thenComparing((CharComparator)second) : Comparator.super.thenComparing(second));
   }

   static <U extends Comparable<? super U>> CharComparator comparing(Char2ObjectFunction<? extends U> keyExtractor) {
      return (CharComparator)((Serializable)((k1, k2) -> ((Comparable)keyExtractor.get(k1)).compareTo(keyExtractor.get(k2))));
   }

   static <U extends Comparable<? super U>> CharComparator comparing(Char2ObjectFunction<? extends U> keyExtractor, Comparator<? super U> keyComparator) {
      Objects.requireNonNull(keyExtractor);
      return (CharComparator)((Serializable)((k1, k2) -> keyComparator.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static CharComparator comparingInt(Char2IntFunction keyExtractor) {
      return (CharComparator)((Serializable)((k1, k2) -> Integer.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static CharComparator comparingLong(Char2LongFunction keyExtractor) {
      return (CharComparator)((Serializable)((k1, k2) -> Long.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }

   static CharComparator comparingDouble(Char2DoubleFunction keyExtractor) {
      return (CharComparator)((Serializable)((k1, k2) -> Double.compare(keyExtractor.get(k1), keyExtractor.get(k2))));
   }
}

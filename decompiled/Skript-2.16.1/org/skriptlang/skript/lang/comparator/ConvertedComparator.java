package org.skriptlang.skript.lang.comparator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.converter.ConverterInfo;

final class ConvertedComparator<T1, T2, C1, C2> implements Comparator<T1, T2> {
   private final ComparatorInfo<C1, C2> comparator;
   @Nullable
   private final ConverterInfo<T1, C1> firstConverter;
   @Nullable
   private final ConverterInfo<T2, C2> secondConverter;

   @Contract("null, _, null -> fail")
   ConvertedComparator(@Nullable ConverterInfo<T1, C1> firstConverter, ComparatorInfo<C1, C2> c, @Nullable ConverterInfo<T2, C2> secondConverter) {
      if (firstConverter == null && secondConverter == null) {
         throw new IllegalArgumentException("firstConverter and secondConverter must not BOTH be null!");
      } else {
         this.firstConverter = firstConverter;
         this.comparator = c;
         this.secondConverter = secondConverter;
      }
   }

   @Override
   public Relation compare(T1 o1, T2 o2) {
      C1 t1 = (C1)(this.firstConverter == null ? o1 : this.firstConverter.getConverter().convert(o1));
      if (t1 == null) {
         return Relation.NOT_EQUAL;
      } else {
         C2 t2 = (C2)(this.secondConverter == null ? o2 : this.secondConverter.getConverter().convert(o2));
         return t2 == null ? Relation.NOT_EQUAL : this.comparator.getComparator().compare(t1, t2);
      }
   }

   @Override
   public boolean supportsOrdering() {
      return this.comparator.getComparator().supportsOrdering();
   }

   @Override
   public String toString() {
      return "ConvertedComparator{first=" + this.firstConverter + ",comparator=" + this.comparator + ",second=" + this.secondConverter + "}";
   }
}

package com.viaversion.viaversion.libs.fastutil.longs;

import com.viaversion.viaversion.libs.fastutil.SortedPair;
import java.io.Serializable;

public interface LongLongSortedPair extends LongLongPair, SortedPair<Long>, Serializable {
   static LongLongSortedPair of(long left, long right) {
      return LongLongImmutableSortedPair.of(left, right);
   }

   default boolean contains(long e) {
      return e == this.leftLong() || e == this.rightLong();
   }

   @Deprecated
   @Override
   default boolean contains(Object o) {
      return o == null ? false : this.contains(((Long)o).longValue());
   }
}

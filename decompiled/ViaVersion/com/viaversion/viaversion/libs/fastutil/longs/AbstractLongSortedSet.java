package com.viaversion.viaversion.libs.fastutil.longs;

public abstract class AbstractLongSortedSet extends AbstractLongSet implements LongSortedSet {
   protected AbstractLongSortedSet() {
   }

   @Override
   public abstract LongBidirectionalIterator iterator();
}

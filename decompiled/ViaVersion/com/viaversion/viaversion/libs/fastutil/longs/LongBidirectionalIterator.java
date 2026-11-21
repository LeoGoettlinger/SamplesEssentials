package com.viaversion.viaversion.libs.fastutil.longs;

import com.viaversion.viaversion.libs.fastutil.objects.ObjectBidirectionalIterator;

public interface LongBidirectionalIterator extends LongIterator, ObjectBidirectionalIterator<Long> {
   long previousLong();

   @Deprecated
   default Long previous() {
      return this.previousLong();
   }

   @Override
   default int back(int n) {
      int i = n;

      while (i-- != 0 && this.hasPrevious()) {
         this.previousLong();
      }

      return n - i - 1;
   }

   @Override
   default int skip(int n) {
      return LongIterator.super.skip(n);
   }
}

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import java.util.NoSuchElementException;
import javax.annotation.CheckForNull;

@ElementTypesAreNonnullByDefault
@GwtCompatible
public abstract class AbstractSequentialIterator<T> extends UnmodifiableIterator<T> {
   @CheckForNull
   private T nextOrNull;

   protected AbstractSequentialIterator(@CheckForNull T firstOrNull) {
      this.nextOrNull = firstOrNull;
   }

   @CheckForNull
   protected abstract T computeNext(T previous);

   @Override
   public final boolean hasNext() {
      return this.nextOrNull != null;
   }

   @Override
   public final T next() {
      if (this.nextOrNull == null) {
         throw new NoSuchElementException();
      } else {
         T oldNext = this.nextOrNull;
         this.nextOrNull = this.computeNext(oldNext);
         return oldNext;
      }
   }
}

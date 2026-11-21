package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.Nullable;

public abstract class NonNullIterator<T> implements Iterator<T> {
   @Nullable
   private T current = (T)null;
   private boolean ended = false;

   @Override
   public final boolean hasNext() {
      if (this.current != null) {
         return true;
      } else if (this.ended) {
         return false;
      } else {
         this.current = this.getNext();
         if (this.current == null) {
            this.ended = true;
         }

         return !this.ended;
      }
   }

   @Nullable
   protected abstract T getNext();

   @Override
   public final T next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         T t = this.current;
         this.current = null;

         assert t != null;

         return t;
      }
   }

   @Override
   public void remove() {
      throw new UnsupportedOperationException();
   }
}

package ch.njol.util.coll.iterator;

import ch.njol.util.NullableChecker;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.Nullable;

public class CheckedIterator<T> implements Iterator<T> {
   private final Iterator<T> iter;
   private final NullableChecker<T> checker;
   private boolean returnedNext = true;
   @Nullable
   private T next;

   public CheckedIterator(Iterator<T> iter, NullableChecker<T> checker) {
      this.iter = iter;
      this.checker = checker;
   }

   @Override
   public boolean hasNext() {
      if (!this.returnedNext) {
         return true;
      } else if (!this.iter.hasNext()) {
         return false;
      } else {
         while (this.iter.hasNext()) {
            this.next = this.iter.next();
            if (this.checker.check(this.next)) {
               this.returnedNext = false;
               return true;
            }
         }

         return false;
      }
   }

   @Nullable
   @Override
   public T next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         this.returnedNext = true;
         return this.next;
      }
   }

   @Override
   public void remove() {
      this.iter.remove();
   }
}

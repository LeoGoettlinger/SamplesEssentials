package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.Nullable;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
public class CombinedIterator<T> implements Iterator<T> {
   private final Iterator<? extends Iterable<T>> iterators;
   private boolean removable;
   @Nullable
   private Iterator<T> current = null;
   @Nullable
   private Iterator<T> last = null;

   public CombinedIterator(Iterator<? extends Iterable<T>> iterators) {
      this(iterators, true);
   }

   public CombinedIterator(Iterator<? extends Iterable<T>> iterators, boolean removable) {
      this.iterators = iterators;
      this.removable = removable;
   }

   @Override
   public boolean hasNext() {
      while ((this.current == null || !this.current.hasNext()) && this.iterators.hasNext()) {
         this.current = this.iterators.next().iterator();
      }

      return this.current != null && this.current.hasNext();
   }

   @Nullable
   @Override
   public T next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         Iterator<T> current = this.current;

         assert current != null;

         this.last = current;
         return current.next();
      }
   }

   @Override
   public void remove() {
      if (!this.removable) {
         throw new UnsupportedOperationException();
      } else if (this.last != null) {
         this.last.remove();
      } else {
         throw new IllegalStateException();
      }
   }
}

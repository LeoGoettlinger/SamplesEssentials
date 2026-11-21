package ch.njol.util.coll.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.jetbrains.annotations.Nullable;

public class ListRangeIterator<T> implements Iterator<T> {
   private final ListIterator<T> iter;
   private int end;

   public ListRangeIterator(List<T> list, int start, int end) {
      ListIterator<T> iter = list.listIterator(start);
      if (iter == null) {
         throw new IllegalArgumentException(list + "");
      } else {
         this.iter = iter;
         this.end = end;
      }
   }

   @Override
   public boolean hasNext() {
      return this.iter.nextIndex() < this.end;
   }

   @Nullable
   @Override
   public T next() {
      if (!this.hasNext()) {
         throw new NoSuchElementException();
      } else {
         return this.iter.next();
      }
   }

   @Override
   public void remove() {
      this.iter.remove();
      this.end--;
   }
}

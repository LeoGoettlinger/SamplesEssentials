package ch.njol.util.coll.iterator;

import java.util.Enumeration;
import java.util.Iterator;
import org.jetbrains.annotations.Nullable;

public class EnumerationIterable<T> implements Iterable<T> {
   @Nullable
   final Enumeration<? extends T> e;

   public EnumerationIterable(@Nullable Enumeration<? extends T> e) {
      this.e = e;
   }

   @Override
   public Iterator<T> iterator() {
      final Enumeration<? extends T> e = this.e;
      return (Iterator<T>)(e == null ? EmptyIterator.get() : new Iterator<T>() {
         @Override
         public boolean hasNext() {
            return e.hasMoreElements();
         }

         @Nullable
         @Override
         public T next() {
            return (T)e.nextElement();
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }
      });
   }
}

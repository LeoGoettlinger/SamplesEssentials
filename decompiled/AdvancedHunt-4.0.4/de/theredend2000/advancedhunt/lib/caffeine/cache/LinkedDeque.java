package de.theredend2000.advancedhunt.lib.caffeine.cache;

import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

interface LinkedDeque<E> extends Deque<E> {
   boolean isFirst(@Nullable E e);

   boolean isLast(@Nullable E e);

   void moveToFront(E e);

   void moveToBack(E e);

   @Nullable
   E getPrevious(E e);

   void setPrevious(E e, @Nullable E prev);

   @Nullable
   E getNext(E e);

   void setNext(E e, @Nullable E next);

   LinkedDeque.PeekingIterator<E> iterator();

   LinkedDeque.PeekingIterator<E> descendingIterator();

   public interface PeekingIterator<E> extends Iterator<E> {
      @Nullable
      E peek();

      static <E> LinkedDeque.PeekingIterator<E> concat(LinkedDeque.PeekingIterator<E> first, LinkedDeque.PeekingIterator<E> second) {
         return new LinkedDeque.PeekingIterator<E>() {
            @Override
            public boolean hasNext() {
               return first.hasNext() || second.hasNext();
            }

            @Override
            public E next() {
               if (first.hasNext()) {
                  return first.next();
               } else if (second.hasNext()) {
                  return second.next();
               } else {
                  throw new NoSuchElementException();
               }
            }

            @Nullable
            @Override
            public E peek() {
               return first.hasNext() ? first.peek() : second.peek();
            }
         };
      }

      static <E> LinkedDeque.PeekingIterator<E> comparing(LinkedDeque.PeekingIterator<E> first, LinkedDeque.PeekingIterator<E> second, Comparator<E> comparator) {
         return new LinkedDeque.PeekingIterator<E>() {
            @Override
            public boolean hasNext() {
               return first.hasNext() || second.hasNext();
            }

            @Override
            public E next() {
               if (!first.hasNext()) {
                  return second.next();
               } else if (!second.hasNext()) {
                  return first.next();
               } else {
                  E o1 = Objects.requireNonNull(first.peek());
                  E o2 = Objects.requireNonNull(second.peek());
                  boolean greaterOrEqual = comparator.compare(o1, o2) >= 0;
                  return greaterOrEqual ? first.next() : second.next();
               }
            }

            @Nullable
            @Override
            public E peek() {
               if (!first.hasNext()) {
                  return second.peek();
               } else if (!second.hasNext()) {
                  return first.peek();
               } else {
                  E o1 = Objects.requireNonNull(first.peek());
                  E o2 = Objects.requireNonNull(second.peek());
                  boolean greaterOrEqual = comparator.compare(o1, o2) >= 0;
                  return greaterOrEqual ? o1 : o2;
               }
            }
         };
      }
   }
}

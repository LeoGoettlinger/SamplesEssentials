package ch.njol.util;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;

public interface OpenCloseable extends AutoCloseable {
   OpenCloseable EMPTY = new OpenCloseable() {
      @Override
      public void open() {
      }

      @Override
      public void close() {
      }
   };

   static OpenCloseable combine(OpenCloseable... openCloseableArray) {
      final Deque<OpenCloseable> openCloseables = new ArrayDeque<>(Arrays.asList(openCloseableArray));
      return new OpenCloseable() {
         @Override
         public void open() {
            for (OpenCloseable openCloseable : openCloseables) {
               openCloseable.open();
            }
         }

         @Override
         public void close() {
            Iterator<OpenCloseable> openCloseableIterator = openCloseables.descendingIterator();

            while (openCloseableIterator.hasNext()) {
               openCloseableIterator.next().close();
            }
         }
      };
   }

   void open();

   @Override
   void close();
}

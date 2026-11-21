package org.jdbi.v3.core.result;

import java.io.Closeable;
import java.util.Iterator;
import java.util.function.Consumer;
import org.jdbi.v3.core.statement.StatementContext;

public interface ResultIterator<T> extends Iterator<T>, Closeable {
   @Override
   default void forEachRemaining(Consumer<? super T> action) {
      try {
         Iterator.super.forEachRemaining(action);
      } finally {
         this.close();
      }
   }

   @Override
   void close();

   StatementContext getContext();
}

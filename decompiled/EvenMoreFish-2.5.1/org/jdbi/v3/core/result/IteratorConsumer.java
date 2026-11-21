package org.jdbi.v3.core.result;

@FunctionalInterface
public interface IteratorConsumer<T, X extends Exception> {
   void useIterator(ResultIterator<T> iterator) throws X;
}

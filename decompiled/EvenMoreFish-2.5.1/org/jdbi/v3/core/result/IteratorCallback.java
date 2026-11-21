package org.jdbi.v3.core.result;

@FunctionalInterface
public interface IteratorCallback<T, R, X extends Exception> {
   R withIterator(ResultIterator<T> iterator) throws X;
}

package org.jdbi.v3.core.internal.exceptions;

@FunctionalInterface
public interface CheckedBiConsumer<X, Y> {
   void accept(X x, Y y) throws Throwable;
}

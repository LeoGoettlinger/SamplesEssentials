package org.jdbi.v3.core.internal.exceptions;

@FunctionalInterface
public interface CheckedBiFunction<X, Y, T> {
   T apply(X x, Y y) throws Throwable;
}

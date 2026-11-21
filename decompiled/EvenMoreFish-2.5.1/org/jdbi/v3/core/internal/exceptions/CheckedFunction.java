package org.jdbi.v3.core.internal.exceptions;

@FunctionalInterface
public interface CheckedFunction<X, T> {
   T apply(X x) throws Throwable;
}

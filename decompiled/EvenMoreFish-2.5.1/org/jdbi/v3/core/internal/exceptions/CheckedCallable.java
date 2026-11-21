package org.jdbi.v3.core.internal.exceptions;

@FunctionalInterface
public interface CheckedCallable<T> {
   T call() throws Throwable;
}

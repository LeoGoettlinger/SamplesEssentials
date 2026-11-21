package org.jdbi.v3.core.internal.exceptions;

@FunctionalInterface
public interface CheckedSupplier<T> {
   T get() throws Throwable;
}

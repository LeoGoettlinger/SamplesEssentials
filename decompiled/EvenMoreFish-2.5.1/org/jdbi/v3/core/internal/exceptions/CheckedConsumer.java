package org.jdbi.v3.core.internal.exceptions;

@FunctionalInterface
public interface CheckedConsumer<T> {
   void accept(T t) throws Exception;
}

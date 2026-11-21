package org.jdbi.v3.core.result;

import java.util.stream.Stream;

@FunctionalInterface
public interface StreamConsumer<T, X extends Exception> {
   void useStream(Stream<T> stream) throws X;
}

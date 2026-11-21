package org.jdbi.v3.core.result;

import java.util.stream.Stream;

@FunctionalInterface
public interface StreamCallback<T, R, X extends Exception> {
   R withStream(Stream<T> stream) throws X;
}

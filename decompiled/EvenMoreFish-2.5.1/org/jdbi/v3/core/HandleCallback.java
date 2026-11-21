package org.jdbi.v3.core;

@FunctionalInterface
public interface HandleCallback<T, X extends Exception> {
   T withHandle(Handle handle) throws X;
}

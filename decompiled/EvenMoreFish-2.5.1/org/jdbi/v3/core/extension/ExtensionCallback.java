package org.jdbi.v3.core.extension;

@FunctionalInterface
public interface ExtensionCallback<R, E, X extends Exception> {
   R withExtension(E extension) throws X;
}

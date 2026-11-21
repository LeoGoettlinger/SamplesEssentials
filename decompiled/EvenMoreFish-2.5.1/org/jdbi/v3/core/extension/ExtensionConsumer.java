package org.jdbi.v3.core.extension;

@FunctionalInterface
public interface ExtensionConsumer<E, X extends Exception> {
   void useExtension(E extension) throws X;
}

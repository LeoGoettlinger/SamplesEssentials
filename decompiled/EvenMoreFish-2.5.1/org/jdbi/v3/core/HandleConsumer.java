package org.jdbi.v3.core;

@FunctionalInterface
public interface HandleConsumer<X extends Exception> {
   void useHandle(Handle handle) throws X;

   default HandleCallback<Void, X> asCallback() {
      return h -> {
         this.useHandle(h);
         return null;
      };
   }
}

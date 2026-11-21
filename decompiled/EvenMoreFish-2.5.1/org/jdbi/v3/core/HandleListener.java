package org.jdbi.v3.core;

public interface HandleListener {
   default void handleCreated(Handle handle) {
   }

   default void handleClosed(Handle handle) {
   }
}

package org.jdbi.v3.core.statement.internal;

public interface OptionalEvent {
   void begin();

   boolean shouldCommit();
}

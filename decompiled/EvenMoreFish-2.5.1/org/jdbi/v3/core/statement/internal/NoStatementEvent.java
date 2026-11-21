package org.jdbi.v3.core.statement.internal;

class NoStatementEvent implements OptionalEvent {
   @Override
   public void begin() {
   }

   @Override
   public boolean shouldCommit() {
      return false;
   }
}

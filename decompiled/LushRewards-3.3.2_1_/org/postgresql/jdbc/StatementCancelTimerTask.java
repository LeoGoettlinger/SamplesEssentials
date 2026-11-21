package org.postgresql.jdbc;

import java.util.TimerTask;
import org.checkerframework.checker.nullness.qual.Nullable;

class StatementCancelTimerTask extends TimerTask {
   @Nullable
   private PgStatement statement;

   StatementCancelTimerTask(PgStatement statement) {
      this.statement = statement;
   }

   @Override
   public boolean cancel() {
      boolean result = super.cancel();
      this.statement = null;
      return result;
   }

   @Override
   public void run() {
      PgStatement statement = this.statement;
      if (statement != null) {
         statement.cancelIfStillNeeded(this);
      }

      this.statement = null;
   }
}

package org.postgresql.core;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.postgresql.util.internal.Nullness;

public class ResultHandlerBase implements ResultHandler {
   @Nullable
   private SQLException firstException;
   @Nullable
   private SQLException lastException;
   @Nullable
   private SQLWarning firstWarning;
   @Nullable
   private SQLWarning lastWarning;

   @Override
   public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
   }

   @Override
   public void handleCommandStatus(String status, long updateCount, long insertOID) {
   }

   @Override
   public void secureProgress() {
   }

   @Override
   public void handleWarning(SQLWarning warning) {
      if (this.firstWarning == null) {
         this.firstWarning = this.lastWarning = warning;
      } else {
         SQLWarning lastWarning = Nullness.castNonNull(this.lastWarning);
         lastWarning.setNextException(warning);
         this.lastWarning = warning;
      }
   }

   @Override
   public void handleError(SQLException error) {
      if (this.firstException == null) {
         this.firstException = this.lastException = error;
      } else {
         Nullness.castNonNull(this.lastException).setNextException(error);
         this.lastException = error;
      }
   }

   @Override
   public void handleCompletion() throws SQLException {
      SQLException firstException = this.firstException;
      if (firstException != null) {
         throw firstException;
      }
   }

   @Nullable
   @Override
   public SQLException getException() {
      return this.firstException;
   }

   @Nullable
   @Override
   public SQLWarning getWarning() {
      return this.firstWarning;
   }
}

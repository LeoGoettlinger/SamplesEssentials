package org.postgresql.core;

import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.List;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ResultHandlerDelegate implements ResultHandler {
   @Nullable
   private final ResultHandler delegate;

   public ResultHandlerDelegate(@Nullable ResultHandler delegate) {
      this.delegate = delegate;
   }

   @Override
   public void handleResultRows(Query fromQuery, Field[] fields, List<Tuple> tuples, @Nullable ResultCursor cursor) {
      if (this.delegate != null) {
         this.delegate.handleResultRows(fromQuery, fields, tuples, cursor);
      }
   }

   @Override
   public void handleCommandStatus(String status, long updateCount, long insertOID) {
      if (this.delegate != null) {
         this.delegate.handleCommandStatus(status, updateCount, insertOID);
      }
   }

   @Override
   public void handleWarning(SQLWarning warning) {
      if (this.delegate != null) {
         this.delegate.handleWarning(warning);
      }
   }

   @Override
   public void handleError(SQLException error) {
      if (this.delegate != null) {
         this.delegate.handleError(error);
      }
   }

   @Override
   public void handleCompletion() throws SQLException {
      if (this.delegate != null) {
         this.delegate.handleCompletion();
      }
   }

   @Override
   public void secureProgress() {
      if (this.delegate != null) {
         this.delegate.secureProgress();
      }
   }

   @Nullable
   @Override
   public SQLException getException() {
      return this.delegate != null ? this.delegate.getException() : null;
   }

   @Nullable
   @Override
   public SQLWarning getWarning() {
      return this.delegate != null ? this.delegate.getWarning() : null;
   }
}

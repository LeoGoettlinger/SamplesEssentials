package org.jdbi.v3.core.transaction;

import java.sql.SQLException;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;

public interface TransactionHandler {
   void begin(Handle handle);

   void commit(Handle handle);

   void rollback(Handle handle);

   boolean isInTransaction(Handle handle);

   void savepoint(Handle handle, String savepointName);

   void rollbackToSavepoint(Handle handle, String savepointName);

   void releaseSavepoint(Handle handle, String savepointName);

   <R, X extends Exception> R inTransaction(Handle handle, HandleCallback<R, X> callback) throws X;

   <R, X extends Exception> R inTransaction(Handle handle, TransactionIsolationLevel level, HandleCallback<R, X> callback) throws X;

   default TransactionHandler specialize(final Handle handle) throws SQLException {
      return this;
   }
}

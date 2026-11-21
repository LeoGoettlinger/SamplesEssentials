package org.jdbi.v3.core.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.HandleCallback;
import org.jdbi.v3.core.internal.exceptions.Unchecked;

public class LocalTransactionHandler implements TransactionHandler {
   private final Map<Handle, LocalTransactionHandler.BoundLocalTransactionHandler> bound = Collections.synchronizedMap(new WeakHashMap<>());
   final boolean managed;

   public LocalTransactionHandler() {
      this(false);
   }

   private LocalTransactionHandler(boolean managed) {
      this.managed = managed;
   }

   @Override
   public void begin(Handle handle) {
      this.nonspecial(handle).begin(handle);
   }

   @Override
   public void commit(Handle handle) {
      this.nonspecial(handle).commit(handle);
   }

   @Override
   public void rollback(Handle handle) {
      this.nonspecial(handle).rollback(handle);
   }

   @Override
   public boolean isInTransaction(Handle handle) {
      return this.nonspecial(handle).isInTransaction(handle);
   }

   @Override
   public void savepoint(Handle handle, String savepointName) {
      this.nonspecial(handle).savepoint(handle, savepointName);
   }

   @Override
   public void rollbackToSavepoint(Handle handle, String savepointName) {
      this.nonspecial(handle).rollbackToSavepoint(handle, savepointName);
   }

   @Override
   public void releaseSavepoint(Handle handle, String savepointName) {
      this.nonspecial(handle).releaseSavepoint(handle, savepointName);
   }

   @Override
   public <R, X extends Exception> R inTransaction(Handle handle, HandleCallback<R, X> callback) throws X {
      return this.nonspecial(handle).inTransaction(handle, callback);
   }

   @Override
   public <R, X extends Exception> R inTransaction(Handle handle, TransactionIsolationLevel level, HandleCallback<R, X> callback) throws X {
      return this.nonspecial(handle).inTransaction(handle, level, callback);
   }

   TransactionHandler nonspecial(Handle handle) {
      return this.bound.computeIfAbsent(handle, Unchecked.function(h -> new LocalTransactionHandler.BoundLocalTransactionHandler(h, this.managed)));
   }

   public static LocalTransactionHandler binding() {
      return new LocalTransactionHandler.BindingLocalTransactionHandler(false);
   }

   public static LocalTransactionHandler managed() {
      return new LocalTransactionHandler.BindingLocalTransactionHandler(true);
   }

   public void reset(Handle handle) {
      this.bound.remove(handle);
   }

   static class BindingLocalTransactionHandler extends LocalTransactionHandler {
      BindingLocalTransactionHandler(boolean managed) {
         super(managed);
      }

      @Override
      public TransactionHandler specialize(Handle handle) throws SQLException {
         return new LocalTransactionHandler.BoundLocalTransactionHandler(handle, this.managed);
      }
   }

   static class BoundLocalTransactionHandler implements TransactionHandler {
      private final Map<String, Savepoint> savepoints = new HashMap<>();
      private final boolean managed;
      private boolean initialAutocommit;
      private LocalTransactionHandler.BoundLocalTransactionHandler.State handlerState;

      BoundLocalTransactionHandler(Handle handle, boolean managed) throws SQLException {
         this.managed = managed;
         this.initialAutocommit = handle.getConnection().getAutoCommit();
         this.handlerState = this.getInitialHandlerState();
      }

      @Override
      public void begin(Handle handle) {
         try {
            if (this.handlerState == LocalTransactionHandler.BoundLocalTransactionHandler.State.OUTSIDE_TRANSACTION) {
               Connection conn = handle.getConnection();
               this.initialAutocommit = conn.getAutoCommit();
               this.savepoints.clear();
               conn.setAutoCommit(false);
               this.handlerState = this.managed
                  ? LocalTransactionHandler.BoundLocalTransactionHandler.State.IN_TRANSACTION
                  : LocalTransactionHandler.BoundLocalTransactionHandler.State.AFTER_BEGIN;
            }
         } catch (SQLException var3) {
            throw new TransactionException("Failed to start transaction", var3);
         }
      }

      @Override
      public void commit(Handle handle) {
         try {
            if (this.handlerState != LocalTransactionHandler.BoundLocalTransactionHandler.State.OUTSIDE_TRANSACTION) {
               handle.getConnection().commit();
            }

            this.handlerState = this.getInitialHandlerState();
            this.restoreAutoCommitState(handle);
         } catch (SQLException var5) {
            try {
               handle.rollback();
            } catch (Exception var4) {
               var5.addSuppressed(var4);
            }

            throw new TransactionException("Failed to commit transaction", var5);
         }
      }

      @Override
      public void rollback(Handle handle) {
         try {
            if (this.handlerState != LocalTransactionHandler.BoundLocalTransactionHandler.State.OUTSIDE_TRANSACTION) {
               handle.getConnection().rollback();
            }
         } catch (SQLException var6) {
            throw new TransactionException("Failed to rollback transaction", var6);
         } finally {
            this.handlerState = this.getInitialHandlerState();
            this.restoreAutoCommitState(handle);
         }
      }

      @Override
      public void savepoint(Handle handle, String name) {
         Connection conn = handle.getConnection();

         try {
            Savepoint savepoint = conn.setSavepoint(name);
            this.savepoints.put(name, savepoint);
         } catch (SQLException var5) {
            throw new TransactionException(String.format("Unable to create savepoint '%s'", name), var5);
         }
      }

      @Override
      public void releaseSavepoint(Handle handle, String name) {
         Connection conn = handle.getConnection();

         try {
            Savepoint savepoint = this.savepoints.remove(name);
            if (savepoint == null) {
               throw new TransactionException(String.format("Attempt to release non-existent savepoint, '%s'", name));
            } else {
               conn.releaseSavepoint(savepoint);
            }
         } catch (SQLException var5) {
            throw new TransactionException(String.format("Unable to create savepoint %s", name), var5);
         }
      }

      @Override
      public void rollbackToSavepoint(Handle handle, String name) {
         Connection conn = handle.getConnection();

         try {
            Savepoint savepoint = this.savepoints.remove(name);
            if (savepoint == null) {
               throw new TransactionException(String.format("Attempt to rollback to non-existent savepoint, '%s'", name));
            } else {
               conn.rollback(savepoint);
            }
         } catch (SQLException var5) {
            throw new TransactionException(String.format("Unable to create savepoint %s", name), var5);
         }
      }

      @Override
      public boolean isInTransaction(Handle handle) {
         if (this.managed) {
            return this.handlerState == LocalTransactionHandler.BoundLocalTransactionHandler.State.IN_TRANSACTION;
         } else {
            try {
               return this.handlerState == LocalTransactionHandler.BoundLocalTransactionHandler.State.IN_TRANSACTION || !handle.getConnection().getAutoCommit();
            } catch (SQLException var3) {
               throw new TransactionException("Failed to test for transaction status", var3);
            }
         }
      }

      @Override
      public <R, X extends Exception> R inTransaction(Handle handle, HandleCallback<R, X> callback) throws X {
         if (this.isInTransaction(handle)) {
            throw new IllegalStateException("Already in transaction");
         } else {
            try {
               handle.begin();
               this.handlerState = LocalTransactionHandler.BoundLocalTransactionHandler.State.IN_TRANSACTION;
               R returnValue = callback.withHandle(handle);
               if (this.handlerState == LocalTransactionHandler.BoundLocalTransactionHandler.State.IN_TRANSACTION) {
                  handle.commit();
               }

               return returnValue;
            } catch (Throwable var7) {
               if (this.handlerState == LocalTransactionHandler.BoundLocalTransactionHandler.State.IN_TRANSACTION) {
                  try {
                     handle.rollback();
                  } catch (Exception var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }
         }
      }

      @Override
      public <R, X extends Exception> R inTransaction(Handle handle, TransactionIsolationLevel level, HandleCallback<R, X> callback) throws X {
         TransactionIsolationLevel initial = handle.getTransactionIsolationLevel();

         Object var5;
         try {
            handle.setTransactionIsolationLevel(level);
            var5 = this.inTransaction(handle, callback);
         } finally {
            handle.setTransactionIsolationLevel(initial);
         }

         return (R)var5;
      }

      private void restoreAutoCommitState(Handle handle) {
         try {
            handle.getConnection().setAutoCommit(this.initialAutocommit);
         } catch (SQLException var6) {
            throw new UnableToRestoreAutoCommitStateException(var6);
         } finally {
            if (this.initialAutocommit) {
               this.savepoints.clear();
            }
         }
      }

      private LocalTransactionHandler.BoundLocalTransactionHandler.State getInitialHandlerState() {
         return !this.managed && !this.initialAutocommit
            ? LocalTransactionHandler.BoundLocalTransactionHandler.State.AFTER_BEGIN
            : LocalTransactionHandler.BoundLocalTransactionHandler.State.OUTSIDE_TRANSACTION;
      }

      static enum State {
         OUTSIDE_TRANSACTION,
         AFTER_BEGIN,
         IN_TRANSACTION;
      }
   }
}

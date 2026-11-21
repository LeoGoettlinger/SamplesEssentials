package org.jdbi.v3.core;

import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.Configurable;
import org.jdbi.v3.core.extension.ExtensionContext;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.jdbi.v3.core.extension.Extensions;
import org.jdbi.v3.core.extension.NoSuchExtensionException;
import org.jdbi.v3.core.internal.exceptions.ThrowableSuppressor;
import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.core.statement.Batch;
import org.jdbi.v3.core.statement.Call;
import org.jdbi.v3.core.statement.Cleanable;
import org.jdbi.v3.core.statement.MetaData;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.jdbi.v3.core.statement.Query;
import org.jdbi.v3.core.statement.Script;
import org.jdbi.v3.core.statement.StatementBuilder;
import org.jdbi.v3.core.statement.Update;
import org.jdbi.v3.core.transaction.TransactionException;
import org.jdbi.v3.core.transaction.TransactionHandler;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.jdbi.v3.core.transaction.UnableToManipulateTransactionIsolationLevelException;
import org.jdbi.v3.core.transaction.UnableToRestoreAutoCommitStateException;
import org.jdbi.v3.meta.Beta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handle implements Closeable, Configurable<Handle> {
   private static final Logger LOG = LoggerFactory.getLogger(Handle.class);
   private final Jdbi jdbi;
   private final Cleanable connectionCleaner;
   private final TransactionHandler transactionHandler;
   private final Connection connection;
   private final boolean forceEndTransactions;
   private StatementBuilder statementBuilder;
   private final ExtensionContext defaultExtensionContext;
   private ExtensionContext currentExtensionContext;
   @GuardedBy("transactionCallbacks")
   private final List<TransactionCallback> transactionCallbacks = new ArrayList<>();
   private final Set<Cleanable> cleanables = new LinkedHashSet<>();
   private final Set<HandleListener> handleListeners;
   private final AtomicBoolean closed = new AtomicBoolean();

   static Handle createHandle(
      Jdbi jdbi, Cleanable connectionCleaner, TransactionHandler transactionHandler, StatementBuilder statementBuilder, Connection connection
   ) throws SQLException {
      Handle handle = new Handle(jdbi, connectionCleaner, transactionHandler, statementBuilder, connection);
      handle.notifyHandleCreated();
      return handle;
   }

   private Handle(Jdbi jdbi, Cleanable connectionCleaner, TransactionHandler transactionHandler, StatementBuilder statementBuilder, Connection connection) throws SQLException {
      this.jdbi = jdbi;
      this.connectionCleaner = connectionCleaner;
      this.connection = connection;
      this.defaultExtensionContext = ExtensionContext.forConfig(jdbi.getConfig().createCopy());
      this.currentExtensionContext = this.defaultExtensionContext;
      this.statementBuilder = statementBuilder;
      this.handleListeners = this.getConfig().get(Handles.class).copyListeners();
      this.addCleanable(() -> statementBuilder.close(connection));
      this.transactionHandler = transactionHandler.specialize(this);
      this.forceEndTransactions = !this.transactionHandler.isInTransaction(this);
   }

   public Jdbi getJdbi() {
      return this.jdbi;
   }

   @Override
   public ConfigRegistry getConfig() {
      return this.currentExtensionContext.getConfig();
   }

   public Connection getConnection() {
      return this.connection;
   }

   public StatementBuilder getStatementBuilder() {
      return this.statementBuilder;
   }

   public Handle setStatementBuilder(StatementBuilder builder) {
      this.statementBuilder = builder;
      return this;
   }

   public Handle addHandleListener(HandleListener handleListener) {
      this.handleListeners.add(handleListener);
      return this;
   }

   public Handle removeHandleListener(HandleListener handleListener) {
      this.handleListeners.remove(handleListener);
      return this;
   }

   public final void addCleanable(Cleanable cleanable) {
      synchronized (this.cleanables) {
         this.cleanables.add(cleanable);
      }
   }

   public final void removeCleanable(Cleanable cleanable) {
      synchronized (this.cleanables) {
         this.cleanables.remove(cleanable);
      }
   }

   @Override
   public void close() {
      if (!this.closed.getAndSet(true)) {
         boolean doForceEndTransactions = this.forceEndTransactions && this.getConfig().get(Handles.class).isForceEndTransactions();

         try {
            ThrowableSuppressor throwableSuppressor = new ThrowableSuppressor();
            this.doClean(throwableSuppressor);

            try {
               this.cleanConnection(doForceEndTransactions);
            } catch (Throwable var7) {
               throwableSuppressor.attachToThrowable(var7);
               throw var7;
            }

            throwableSuppressor.throwIfNecessary(t -> new CloseException("While closing handle", t));
         } finally {
            LOG.trace("Handle [{}] released", this);
            this.notifyHandleClosed();
         }
      }
   }

   public void clean() {
      ThrowableSuppressor throwableSuppressor = new ThrowableSuppressor();
      this.doClean(throwableSuppressor);
      throwableSuppressor.throwIfNecessary();
   }

   public boolean isClean() {
      synchronized (this.cleanables) {
         return this.cleanables.isEmpty();
      }
   }

   private void doClean(ThrowableSuppressor throwableSuppressor) {
      List<Cleanable> cleanablesCopy;
      synchronized (this.cleanables) {
         cleanablesCopy = new ArrayList<>(this.cleanables);
         this.cleanables.clear();
      }

      Collections.reverse(cleanablesCopy);

      for (Cleanable cleanable : cleanablesCopy) {
         throwableSuppressor.suppressAppend(cleanable::close);
      }
   }

   public boolean isClosed() {
      return this.closed.get();
   }

   public Query select(CharSequence sql, Object... args) {
      Query query = this.createQuery(sql);
      int position = 0;

      for (Object arg : args) {
         query.bind(position++, arg);
      }

      return query;
   }

   public Query select(String sql, Object... args) {
      return this.select((CharSequence)sql, args);
   }

   public int execute(CharSequence sql, Object... args) {
      int var11;
      try (Update stmt = this.createUpdate(sql)) {
         int position = 0;

         for (Object arg : args) {
            stmt.bind(position++, arg);
         }

         var11 = stmt.execute();
      }

      return var11;
   }

   public int execute(String sql, Object... args) {
      return this.execute((CharSequence)sql, args);
   }

   public Batch createBatch() {
      return new Batch(this);
   }

   public PreparedBatch prepareBatch(CharSequence sql) {
      return new PreparedBatch(this, sql);
   }

   public PreparedBatch prepareBatch(String sql) {
      return this.prepareBatch((CharSequence)sql);
   }

   public Call createCall(CharSequence sql) {
      return new Call(this, sql);
   }

   public Call createCall(String sql) {
      return this.createCall((CharSequence)sql);
   }

   public Query createQuery(CharSequence sql) {
      return new Query(this, sql);
   }

   public Query createQuery(String sql) {
      return this.createQuery((CharSequence)sql);
   }

   public Script createScript(CharSequence sql) {
      return new Script(this, sql);
   }

   public Script createScript(String sql) {
      return this.createScript((CharSequence)sql);
   }

   public Update createUpdate(CharSequence sql) {
      return new Update(this, sql);
   }

   public Update createUpdate(String sql) {
      return this.createUpdate((CharSequence)sql);
   }

   public ResultBearing queryMetadata(MetaData.MetaDataResultSetProvider metadataFunction) {
      return new MetaData(this, metadataFunction);
   }

   public <T> T queryMetadata(MetaData.MetaDataValueProvider<T> metadataFunction) {
      Object var3;
      try (MetaData metadata = new MetaData(this, metadataFunction)) {
         var3 = metadata.execute();
      }

      return (T)var3;
   }

   public boolean isInTransaction() {
      return this.transactionHandler.isInTransaction(this);
   }

   public Handle begin() {
      this.transactionHandler.begin(this);
      LOG.trace("Handle [{}] begin transaction", this);
      return this;
   }

   public Handle commit() {
      long start = System.nanoTime();

      try {
         this.transactionHandler.commit(this);
      } catch (UnableToRestoreAutoCommitStateException var4) {
         LOG.trace("Handle [{}] commit transaction in {}ms, autocommit restore failed", this, msSince(start));
         this.drainCallbacks(TransactionCallback::afterCommit, var4);
         throw var4;
      }

      LOG.trace("Handle [{}] commit transaction in {}ms", this, msSince(start));
      this.drainCallbacks().forEach(TransactionCallback::afterCommit);
      return this;
   }

   public Handle rollback() {
      long start = System.nanoTime();

      try {
         this.transactionHandler.rollback(this);
      } catch (RuntimeException var4) {
         LOG.trace("Handle [{}] rollback transaction failed in {}ms", this, msSince(start));
         this.drainCallbacks(TransactionCallback::afterRollback, var4);
         throw var4;
      }

      LOG.trace("Handle [{}] rollback transaction in {}ms", this, msSince(start));
      this.drainCallbacks().forEach(TransactionCallback::afterRollback);
      return this;
   }

   @Beta
   public Handle afterCommit(Runnable afterCommit) {
      return this.addTransactionCallback(new TransactionCallback() {
         @Override
         public void afterCommit() {
            afterCommit.run();
         }
      });
   }

   @Beta
   public Handle afterRollback(Runnable afterRollback) {
      return this.addTransactionCallback(new TransactionCallback() {
         @Override
         public void afterRollback() {
            afterRollback.run();
         }
      });
   }

   List<TransactionCallback> drainCallbacks() {
      synchronized (this.transactionCallbacks) {
         List<TransactionCallback> result = new ArrayList<>(this.transactionCallbacks);
         this.transactionCallbacks.clear();
         return result;
      }
   }

   private void drainCallbacks(Consumer<TransactionCallback> action, Throwable failure) {
      for (TransactionCallback callback : this.drainCallbacks()) {
         try {
            action.accept(callback);
         } catch (Throwable var6) {
            failure.addSuppressed(var6);
         }
      }
   }

   Handle addTransactionCallback(TransactionCallback cb) {
      if (!this.isInTransaction()) {
         throw new IllegalStateException("Handle must be in transaction");
      } else {
         synchronized (this.transactionCallbacks) {
            this.transactionCallbacks.add(cb);
            return this;
         }
      }
   }

   public Handle rollbackToSavepoint(String savepointName) {
      long start = System.nanoTime();
      this.transactionHandler.rollbackToSavepoint(this, savepointName);
      LOG.trace("Handle [{}] rollback to savepoint \"{}\" in {}ms", this, savepointName, msSince(start));
      return this;
   }

   private static long msSince(final long start) {
      return TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
   }

   public Handle savepoint(String name) {
      this.transactionHandler.savepoint(this, name);
      LOG.trace("Handle [{}] savepoint \"{}\"", this, name);
      return this;
   }

   @Deprecated(
      since = "3.35.0",
      forRemoval = true
   )
   public Handle release(String savepointName) {
      return this.releaseSavepoint(savepointName);
   }

   public Handle releaseSavepoint(String savepointName) {
      this.transactionHandler.releaseSavepoint(this, savepointName);
      LOG.trace("Handle [{}] release savepoint \"{}\"", this, savepointName);
      return this;
   }

   public boolean isReadOnly() {
      try {
         return this.connection.isReadOnly();
      } catch (SQLException var2) {
         throw new UnableToManipulateTransactionIsolationLevelException("Could not get read-only status for a connection", var2);
      }
   }

   public Handle setReadOnly(boolean readOnly) {
      try {
         this.connection.setReadOnly(readOnly);
         return this;
      } catch (SQLException var3) {
         throw new UnableToManipulateTransactionIsolationLevelException("Could not setReadOnly", var3);
      }
   }

   public <R, X extends Exception> R inTransaction(HandleCallback<R, X> callback) throws X {
      return this.isInTransaction() ? callback.withHandle(this) : this.transactionHandler.inTransaction(this, callback);
   }

   public <X extends Exception> void useTransaction(final HandleConsumer<X> consumer) throws X {
      this.inTransaction(consumer.asCallback());
   }

   public <R, X extends Exception> R inTransaction(TransactionIsolationLevel level, HandleCallback<R, X> callback) throws X {
      if (this.isInTransaction()) {
         TransactionIsolationLevel currentLevel = this.getTransactionIsolationLevel();
         if (currentLevel != level && level != TransactionIsolationLevel.UNKNOWN) {
            throw new TransactionException(
               "Tried to execute nested transaction with isolation level "
                  + level
                  + ", but already running in a transaction with isolation level "
                  + currentLevel
                  + "."
            );
         } else {
            return callback.withHandle(this);
         }
      } else {
         Object var4;
         try (Handle.SetTransactionIsolation isolation = new Handle.SetTransactionIsolation(level)) {
            var4 = this.transactionHandler.inTransaction(this, level, callback);
         }

         return (R)var4;
      }
   }

   public <X extends Exception> void useTransaction(TransactionIsolationLevel level, HandleConsumer<X> consumer) throws X {
      this.inTransaction(level, consumer.asCallback());
   }

   @Deprecated(
      since = "3.35.0",
      forRemoval = true
   )
   public void setTransactionIsolation(TransactionIsolationLevel level) {
      this.setTransactionIsolationLevel(level);
   }

   public void setTransactionIsolationLevel(TransactionIsolationLevel level) {
      if (level != TransactionIsolationLevel.UNKNOWN) {
         this.setTransactionIsolationLevel(level.intValue());
      }
   }

   @Deprecated(
      since = "3.35.0",
      forRemoval = true
   )
   public void setTransactionIsolation(int level) {
      this.setTransactionIsolationLevel(level);
   }

   public void setTransactionIsolationLevel(int level) {
      try {
         if (this.connection.getTransactionIsolation() != level) {
            this.connection.setTransactionIsolation(level);
         }
      } catch (SQLException var3) {
         throw new UnableToManipulateTransactionIsolationLevelException(level, var3);
      }
   }

   public TransactionIsolationLevel getTransactionIsolationLevel() {
      try {
         return TransactionIsolationLevel.valueOf(this.connection.getTransactionIsolation());
      } catch (SQLException var2) {
         throw new UnableToManipulateTransactionIsolationLevelException("unable to access current setting", var2);
      }
   }

   public <T> T attach(Class<T> extensionType) {
      return this.getConfig(Extensions.class)
         .findFor(extensionType, ConstantHandleSupplier.of(this))
         .orElseThrow(() -> new NoSuchExtensionException(extensionType));
   }

   public ExtensionMethod getExtensionMethod() {
      return this.currentExtensionContext.getExtensionMethod();
   }

   Handle acceptExtensionContext(ExtensionContext extensionContext) {
      this.currentExtensionContext = extensionContext == null ? this.defaultExtensionContext : extensionContext;
      return this;
   }

   private void notifyHandleCreated() {
      this.handleListeners.forEach(listener -> listener.handleCreated(this));
   }

   private void notifyHandleClosed() {
      this.handleListeners.forEach(listener -> listener.handleClosed(this));
   }

   private void cleanConnection(boolean doForceEndTransactions) {
      ThrowableSuppressor throwableSuppressor = new ThrowableSuppressor();
      boolean wasInTransaction = false;
      if (doForceEndTransactions) {
         wasInTransaction = throwableSuppressor.suppressAppend(() -> !this.connection.isClosed() && this.isInTransaction(), false);
      }

      if (wasInTransaction) {
         throwableSuppressor.suppressAppend(this::rollback);
      }

      try {
         this.connectionCleaner.close();
      } catch (SQLException var6) {
         CloseException ce = new CloseException("Unable to close Connection", var6);
         throwableSuppressor.attachToThrowable(ce);
         throw ce;
      }

      throwableSuppressor.throwIfNecessary(t -> new CloseException("Failed to clear transaction status on close", t));
      if (wasInTransaction) {
         throw new TransactionException(
            "Improper transaction handling detected: A Handle with an open transaction was closed. Transactions must be explicitly committed or rolled back before closing the Handle. Jdbi has rolled back this transaction automatically.This check may be disabled by calling getConfig(Handles.class).setForceEndTransactions(false)."
         );
      }
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Handle handle = (Handle)o;
         return this.jdbi.equals(handle.jdbi) && this.connection.equals(handle.connection);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.jdbi, this.connection);
   }

   class SetTransactionIsolation implements AutoCloseable {
      private final TransactionIsolationLevel prevLevel = Handle.this.getTransactionIsolationLevel();

      SetTransactionIsolation(TransactionIsolationLevel setLevel) {
         Handle.this.setTransactionIsolationLevel(setLevel);
      }

      @Override
      public void close() {
         Handle.this.setTransactionIsolationLevel(this.prevLevel);
      }
   }
}

package org.jdbi.v3.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.config.Configurable;
import org.jdbi.v3.core.extension.ExtensionCallback;
import org.jdbi.v3.core.extension.ExtensionConsumer;
import org.jdbi.v3.core.extension.Extensions;
import org.jdbi.v3.core.extension.HandleSupplier;
import org.jdbi.v3.core.extension.NoSuchExtensionException;
import org.jdbi.v3.core.internal.OnDemandExtensions;
import org.jdbi.v3.core.internal.exceptions.Unchecked;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.core.statement.DefaultStatementBuilder;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.core.statement.StatementBuilder;
import org.jdbi.v3.core.statement.StatementBuilderFactory;
import org.jdbi.v3.core.transaction.LocalTransactionHandler;
import org.jdbi.v3.core.transaction.TransactionHandler;
import org.jdbi.v3.core.transaction.TransactionIsolationLevel;
import org.jdbi.v3.meta.Alpha;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Jdbi implements Configurable<Jdbi> {
   private static final Logger LOG = LoggerFactory.getLogger(Jdbi.class);
   private final ConfigRegistry config = new ConfigRegistry();
   private final ConnectionFactory connectionFactory;
   private final AtomicReference<TransactionHandler> transactionhandler = new AtomicReference<>(LocalTransactionHandler.binding());
   private final AtomicReference<StatementBuilderFactory> statementBuilderFactory = new AtomicReference<>(DefaultStatementBuilder.FACTORY);
   private final AtomicReference<HandleCallbackDecorator> handleCallbackDecorator = new AtomicReference<>(
      HandleCallbackDecorator.STANDARD_HANDLE_CALLBACK_DECORATOR
   );
   private HandleScope handleScope = HandleScope.threadLocal();
   private final CopyOnWriteArrayList<JdbiPlugin> plugins = new CopyOnWriteArrayList<>();

   private Jdbi(ConnectionFactory connectionFactory) {
      Objects.requireNonNull(connectionFactory, "null connectionFactory");
      this.connectionFactory = connectionFactory;
   }

   public static Jdbi create(Connection connection) {
      return create(new SingleConnectionFactory(connection));
   }

   public static Jdbi create(DataSource dataSource) {
      return create(dataSource::getConnection);
   }

   public static Jdbi create(ConnectionFactory connectionFactory) {
      return new Jdbi(connectionFactory);
   }

   public static Jdbi create(final String url) {
      Objects.requireNonNull(url, "null url");
      return create(() -> DriverManager.getConnection(url));
   }

   public static Jdbi create(final String url, final Properties properties) {
      Objects.requireNonNull(url, "null url");
      Objects.requireNonNull(properties, "null properties");
      return create(() -> DriverManager.getConnection(url, properties));
   }

   public static Jdbi create(final String url, final String username, final String password) {
      Objects.requireNonNull(url, "null url");
      Objects.requireNonNull(username, "null username");
      Objects.requireNonNull(password, "null password");
      return create(() -> DriverManager.getConnection(url, username, password));
   }

   public static Handle open(DataSource dataSource) {
      return create(dataSource).open();
   }

   public static Handle open(ConnectionFactory connectionFactory) {
      return create(connectionFactory).open();
   }

   public static Handle open(final Connection connection) {
      Objects.requireNonNull(connection, "null connection");
      return create(() -> connection).open();
   }

   public static Handle open(final String url) {
      return create(url).open();
   }

   public static Handle open(final String url, final String username, final String password) {
      return create(url, username, password).open();
   }

   public static Handle open(final String url, final Properties props) {
      return create(url, props).open();
   }

   @Deprecated(
      forRemoval = true
   )
   public Jdbi installPlugins() {
      ServiceLoader.load(JdbiPlugin.class).forEach(this::installPlugin);
      LOG.debug("Automatically installed plugins {}", this.plugins);
      return this;
   }

   public Jdbi installPlugin(JdbiPlugin plugin) {
      if (this.plugins.addIfAbsent(plugin)) {
         Unchecked.consumer(plugin::customizeJdbi).accept(this);
      }

      return this;
   }

   public Jdbi setStatementBuilderFactory(StatementBuilderFactory factory) {
      this.statementBuilderFactory.set(factory);
      return this;
   }

   public StatementBuilderFactory getStatementBuilderFactory() {
      return this.statementBuilderFactory.get();
   }

   @Override
   public ConfigRegistry getConfig() {
      return this.config;
   }

   public Jdbi setTransactionHandler(TransactionHandler handler) {
      Objects.requireNonNull(handler, "null transaction handler");
      this.transactionhandler.set(handler);
      return this;
   }

   public TransactionHandler getTransactionHandler() {
      return this.transactionhandler.get();
   }

   @Alpha
   public Jdbi setHandleCallbackDecorator(HandleCallbackDecorator handleCallbackDecorator) {
      Objects.requireNonNull(handleCallbackDecorator, "null handler");
      this.handleCallbackDecorator.set(handleCallbackDecorator);
      return this;
   }

   @Alpha
   public HandleCallbackDecorator getHandleCallbackDecorator() {
      return this.handleCallbackDecorator.get();
   }

   public final HandleScope getHandleScope() {
      return this.handleScope;
   }

   @Alpha
   public final void setHandleScope(HandleScope handleScope) {
      this.handleScope = handleScope;
   }

   public Handle open() {
      try {
         long start = System.nanoTime();
         Connection conn = Objects.requireNonNull(
            this.connectionFactory.openConnection(), () -> "Connection factory " + this.connectionFactory + " returned a null connection"
         );
         long stop = System.nanoTime();

         try {
            for (JdbiPlugin p : this.plugins) {
               conn = p.customizeConnection(conn);
            }

            StatementBuilder cache = this.statementBuilderFactory.get().createStatementBuilder(conn);
            Handle h = Handle.createHandle(this, this.connectionFactory.getCleanableFor(conn), this.transactionhandler.get(), cache, conn);

            for (JdbiPlugin p : this.plugins) {
               h = p.customizeHandle(h);
            }

            LOG.trace("Jdbi [{}] obtain handle [{}] in {}ms", this, h, TimeUnit.MILLISECONDS.convert(stop - start, TimeUnit.NANOSECONDS));
            return h;
         } catch (Throwable var10) {
            this.connectionFactory.getCleanableFor(conn).closeAndSuppress(var10);
            throw var10;
         }
      } catch (SQLException var11) {
         throw new ConnectionException(var11);
      }
   }

   public <R, X extends Exception> R withHandle(HandleCallback<R, X> callback) throws X {
      HandleCallback<R, X> decoratedCallback = this.handleCallbackDecorator.get().decorate(callback);
      HandleSupplier handleSupplier = this.handleScope.get();
      if (handleSupplier != null) {
         return decoratedCallback.withHandle(handleSupplier.getHandle());
      } else {
         Object var6;
         try (Handle h = this.open()) {
            SqlStatements sqlStatements = h.getConfig(SqlStatements.class);
            sqlStatements.setAttachAllStatementsForCleanup(sqlStatements.isAttachCallbackStatementsForCleanup());
            this.handleScope.set(ConstantHandleSupplier.of(h));
            var6 = decoratedCallback.withHandle(h);
         } finally {
            this.handleScope.clear();
         }

         return (R)var6;
      }
   }

   public <X extends Exception> void useHandle(final HandleConsumer<X> consumer) throws X {
      this.withHandle(consumer.asCallback());
   }

   public <R, X extends Exception> R inTransaction(final HandleCallback<R, X> callback) throws X {
      return this.withHandle(handle -> handle.inTransaction(callback));
   }

   public <X extends Exception> void useTransaction(final HandleConsumer<X> callback) throws X {
      this.useHandle(handle -> handle.useTransaction(callback));
   }

   public <R, X extends Exception> R inTransaction(final TransactionIsolationLevel level, final HandleCallback<R, X> callback) throws X {
      return this.withHandle(handle -> handle.inTransaction(level, callback));
   }

   public <X extends Exception> void useTransaction(final TransactionIsolationLevel level, final HandleConsumer<X> callback) throws X {
      this.useHandle(handle -> handle.useTransaction(level, callback));
   }

   public <R, E, X extends Exception> R withExtension(Class<E> extensionType, ExtensionCallback<R, E, X> callback) throws X {
      HandleSupplier handleSupplier = this.handleScope.get();
      if (handleSupplier != null) {
         return this.callWithExtension(extensionType, callback, handleSupplier);
      } else {
         Object var5;
         try (LazyHandleSupplier lazyHandleSupplier = new LazyHandleSupplier(this)) {
            this.handleScope.set(lazyHandleSupplier);
            var5 = this.callWithExtension(extensionType, callback, lazyHandleSupplier);
         } finally {
            this.handleScope.clear();
         }

         return (R)var5;
      }
   }

   private <R, E, X extends Exception> R callWithExtension(Class<E> extensionType, ExtensionCallback<R, E, X> callback, HandleSupplier handleSupplier) throws X {
      E extension = this.getConfig(Extensions.class).findFor(extensionType, handleSupplier).orElseThrow(() -> new NoSuchExtensionException(extensionType));
      return callback.withExtension(extension);
   }

   public <E, X extends Exception> void useExtension(Class<E> extensionType, ExtensionConsumer<E, X> callback) throws X {
      this.withExtension(extensionType, extension -> {
         callback.useExtension((E)extension);
         return null;
      });
   }

   public <E> E onDemand(Class<E> extensionType) {
      if (!extensionType.isInterface()) {
         throw new IllegalArgumentException("On-demand extensions are only supported for interfaces.");
      } else if (!this.getConfig(Extensions.class).hasExtensionFor(extensionType)) {
         throw new NoSuchExtensionException(extensionType);
      } else {
         return this.getConfig(OnDemandExtensions.class).create(this, extensionType);
      }
   }
}

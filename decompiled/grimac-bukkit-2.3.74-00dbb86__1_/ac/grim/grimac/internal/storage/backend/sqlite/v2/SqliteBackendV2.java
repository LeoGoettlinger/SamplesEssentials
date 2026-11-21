package ac.grim.grimac.internal.storage.backend.sqlite.v2;

import ac.grim.grimac.api.storage.backend.AdminAdapter;
import ac.grim.grimac.api.storage.backend.ApiVersion;
import ac.grim.grimac.api.storage.backend.BackendContext;
import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.backend.BackendV2;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.backend.SearchAdapter;
import ac.grim.grimac.api.storage.backend.TxAdapter;
import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.kind.Counter;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.kind.Entity;
import ac.grim.grimac.api.storage.kind.EventStream;
import ac.grim.grimac.api.storage.kind.KeyValueScoped;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlCounterAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlEntityAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlEventStreamAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlKeyValueScopedAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlServerOwnershipAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.dialect.SqliteDialect;
import ac.grim.grimac.internal.storage.backend.sqlite.SqliteBackendConfig;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import javax.sql.DataSource;

@ApiStatus.Internal
public final class SqliteBackendV2 implements BackendV2 {
   @NotNull
   private final SqliteBackendConfig config;
   private final SqliteDialect dialectOverride;
   private SqliteDialect dialect;
   private Logger logger;
   private Connection connection;
   private DataSource singleConnDs;
   private SqlEntityAdapter entityAdapter;
   private SqlServerOwnershipAdapter ownershipAdapter;
   private SqlEventStreamAdapter eventStreamAdapter;
   private SqlKeyValueScopedAdapter kvScopedAdapter;
   private SqlCounterAdapter counterAdapter;

   public SqliteBackendV2(@NotNull SqliteBackendConfig config) {
      this(config, null);
   }

   @ApiStatus.Internal
   public SqliteBackendV2(@NotNull SqliteBackendConfig config, SqliteDialect dialectOverride) {
      this.config = config;
      this.dialectOverride = dialectOverride;
   }

   @NotNull
   @Override
   public String id() {
      return "sqlite-v2";
   }

   @NotNull
   @Override
   public ApiVersion apiVersion() {
      return ApiVersion.CURRENT;
   }

   @Override
   public int writerThreads(@NotNull Category<?> category) {
      return 1;
   }

   @NotNull
   @Override
   public EnumSet<Capability> capabilities() {
      return EnumSet.of(Capability.KIND_ENTITY, Capability.KIND_EVENT_STREAM, Capability.KIND_KV_SCOPED, Capability.KIND_COUNTER);
   }

   @Override
   public void init(@NotNull BackendContext ctx) throws BackendException {
      this.logger = ctx.logger();
      Path dbFile = ctx.dataDirectory().resolve(this.config.path());
      String jdbcUrl = "jdbc:sqlite:" + dbFile.toAbsolutePath();

      try {
         Class.forName("org.sqlite.JDBC");
      } catch (ClassNotFoundException var11) {
         throw new BackendException("SQLite JDBC driver missing: " + var11.getMessage(), var11);
      }

      Path parent = dbFile.getParent();
      if (parent != null) {
         try {
            Files.createDirectories(parent);
         } catch (IOException var10) {
            throw new BackendException("failed to create SQLite database directory " + parent, var10);
         }
      }

      try {
         this.connection = DriverManager.getConnection(jdbcUrl);

         try (Statement s = this.connection.createStatement()) {
            s.execute("PRAGMA journal_mode=WAL");
         }

         try (Statement s = this.connection.createStatement()) {
            s.execute("PRAGMA busy_timeout=30000");
         }

         this.dialect = this.dialectOverride != null ? this.dialectOverride : selectDialect(this.connection, this.logger);
      } catch (SQLException var14) {
         throw new BackendException("failed to open SQLite database " + dbFile, var14);
      }

      this.singleConnDs = new SqliteBackendV2.SingleConnectionDataSource(this.connection);
      this.entityAdapter = new SqlEntityAdapter(this.singleConnDs, this.dialect, this.logger);
      this.ownershipAdapter = new SqlServerOwnershipAdapter(this.singleConnDs, this.dialect, this.logger);
      this.eventStreamAdapter = new SqlEventStreamAdapter(this.singleConnDs, this.dialect, this.logger);
      this.kvScopedAdapter = new SqlKeyValueScopedAdapter(this.singleConnDs, this.dialect, this.logger);
      this.counterAdapter = new SqlCounterAdapter(this.singleConnDs, this.dialect, this.logger);
   }

   @NotNull
   private static SqliteDialect selectDialect(@NotNull Connection connection, @NotNull Logger logger) throws SQLException {
      String version;
      try (
         Statement s = connection.createStatement();
         ResultSet rs = s.executeQuery("SELECT sqlite_version()");
      ) {
         if (!rs.next()) {
            logger.warning("[grim-datastore] sqlite_version() returned no row; assuming modern SQLite dialect");
            return new SqliteDialect();
         }

         version = rs.getString(1);
      }

      SqliteDialect selected = SqliteDialect.forEngineVersion(version);
      logger.info(
         "[grim-datastore] SQLite engine "
            + version
            + " — using "
            + (selected.usesLegacySqliteUpsert() ? "legacy (pre-3.24, no UPSERT) dialect" : "modern dialect")
            + (selected.supportsReturning() ? "" : " without RETURNING")
      );
      return selected;
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() throws BackendException {
      if (this.connection != null) {
         try {
            this.connection.close();
         } catch (SQLException var2) {
            throw new BackendException("SQLite close failed", var2);
         }
      }
   }

   @NotNull
   @Override
   public <K extends DataKind<?, ?>> Optional<KindAdapter<K>> adapterFor(@NotNull K kind) {
      if (kind instanceof Entity) {
         return Optional.of(this.entityAdapter);
      } else if (kind instanceof EventStream) {
         return Optional.of(this.eventStreamAdapter);
      } else if (kind instanceof KeyValueScoped) {
         return Optional.of(this.kvScopedAdapter);
      } else {
         return kind instanceof Counter ? Optional.of(this.counterAdapter) : Optional.empty();
      }
   }

   @NotNull
   @Override
   public Optional<SearchAdapter> searchAdapter() {
      return Optional.empty();
   }

   @NotNull
   @Override
   public Optional<TxAdapter> txAdapter() {
      return Optional.empty();
   }

   @NotNull
   @Override
   public Optional<AdminAdapter> adminAdapter() {
      return Optional.empty();
   }

   @NotNull
   @Override
   public Optional<ServerOwnershipAdapter> ownershipAdapter() {
      return Optional.ofNullable(this.ownershipAdapter);
   }

   @NotNull
   @Override
   public <X> Optional<X> unwrap(@NotNull Class<X> type) {
      return type.isAssignableFrom(DataSource.class) && this.singleConnDs != null ? Optional.of((X)this.singleConnDs) : Optional.empty();
   }

   private static final class NonClosingConnection implements Connection {
      private final Connection delegate;
      private final ReentrantLock lock;
      private boolean closed;

      NonClosingConnection(Connection delegate, ReentrantLock lock) {
         this.delegate = delegate;
         this.lock = lock;
      }

      @Override
      public void close() {
         if (!this.closed) {
            this.closed = true;
            this.lock.unlock();
         }
      }

      @Override
      public Statement createStatement() throws SQLException {
         return this.delegate.createStatement();
      }

      @Override
      public PreparedStatement prepareStatement(String sql) throws SQLException {
         return this.delegate.prepareStatement(sql);
      }

      @Override
      public boolean getAutoCommit() throws SQLException {
         return this.delegate.getAutoCommit();
      }

      @Override
      public void setAutoCommit(boolean a) throws SQLException {
         this.delegate.setAutoCommit(a);
      }

      @Override
      public void commit() throws SQLException {
         this.delegate.commit();
      }

      @Override
      public void rollback() throws SQLException {
         this.delegate.rollback();
      }

      @Override
      public boolean isClosed() throws SQLException {
         return this.delegate.isClosed();
      }

      @Override
      public boolean isValid(int timeout) throws SQLException {
         return this.delegate.isValid(timeout);
      }

      @Override
      public DatabaseMetaData getMetaData() throws SQLException {
         return this.delegate.getMetaData();
      }

      @Override
      public void setReadOnly(boolean r) throws SQLException {
         this.delegate.setReadOnly(r);
      }

      @Override
      public boolean isReadOnly() throws SQLException {
         return this.delegate.isReadOnly();
      }

      @Override
      public void setCatalog(String c) throws SQLException {
         this.delegate.setCatalog(c);
      }

      @Override
      public String getCatalog() throws SQLException {
         return this.delegate.getCatalog();
      }

      @Override
      public void setTransactionIsolation(int l) throws SQLException {
         this.delegate.setTransactionIsolation(l);
      }

      @Override
      public int getTransactionIsolation() throws SQLException {
         return this.delegate.getTransactionIsolation();
      }

      @Override
      public SQLWarning getWarnings() throws SQLException {
         return this.delegate.getWarnings();
      }

      @Override
      public void clearWarnings() throws SQLException {
         this.delegate.clearWarnings();
      }

      @Override
      public Statement createStatement(int t, int c) throws SQLException {
         return this.delegate.createStatement(t, c);
      }

      @Override
      public PreparedStatement prepareStatement(String s, int t, int c) throws SQLException {
         return this.delegate.prepareStatement(s, t, c);
      }

      @Override
      public CallableStatement prepareCall(String s) throws SQLException {
         return this.delegate.prepareCall(s);
      }

      @Override
      public CallableStatement prepareCall(String s, int t, int c) throws SQLException {
         return this.delegate.prepareCall(s, t, c);
      }

      @Override
      public String nativeSQL(String s) throws SQLException {
         return this.delegate.nativeSQL(s);
      }

      @Override
      public Map<String, Class<?>> getTypeMap() throws SQLException {
         return this.delegate.getTypeMap();
      }

      @Override
      public void setTypeMap(Map<String, Class<?>> m) throws SQLException {
         this.delegate.setTypeMap(m);
      }

      @Override
      public void setHoldability(int h) throws SQLException {
         this.delegate.setHoldability(h);
      }

      @Override
      public int getHoldability() throws SQLException {
         return this.delegate.getHoldability();
      }

      @Override
      public Savepoint setSavepoint() throws SQLException {
         return this.delegate.setSavepoint();
      }

      @Override
      public Savepoint setSavepoint(String n) throws SQLException {
         return this.delegate.setSavepoint(n);
      }

      @Override
      public void rollback(Savepoint s) throws SQLException {
         this.delegate.rollback(s);
      }

      @Override
      public void releaseSavepoint(Savepoint s) throws SQLException {
         this.delegate.releaseSavepoint(s);
      }

      @Override
      public Statement createStatement(int t, int c, int h) throws SQLException {
         return this.delegate.createStatement(t, c, h);
      }

      @Override
      public PreparedStatement prepareStatement(String s, int t, int c, int h) throws SQLException {
         return this.delegate.prepareStatement(s, t, c, h);
      }

      @Override
      public CallableStatement prepareCall(String s, int t, int c, int h) throws SQLException {
         return this.delegate.prepareCall(s, t, c, h);
      }

      @Override
      public PreparedStatement prepareStatement(String s, int f) throws SQLException {
         return this.delegate.prepareStatement(s, f);
      }

      @Override
      public PreparedStatement prepareStatement(String s, int[] i) throws SQLException {
         return this.delegate.prepareStatement(s, i);
      }

      @Override
      public PreparedStatement prepareStatement(String s, String[] n) throws SQLException {
         return this.delegate.prepareStatement(s, n);
      }

      @Override
      public Clob createClob() throws SQLException {
         return this.delegate.createClob();
      }

      @Override
      public Blob createBlob() throws SQLException {
         return this.delegate.createBlob();
      }

      @Override
      public NClob createNClob() throws SQLException {
         return this.delegate.createNClob();
      }

      @Override
      public SQLXML createSQLXML() throws SQLException {
         return this.delegate.createSQLXML();
      }

      @Override
      public void setClientInfo(String k, String v) throws SQLClientInfoException {
         this.delegate.setClientInfo(k, v);
      }

      @Override
      public void setClientInfo(Properties p) throws SQLClientInfoException {
         this.delegate.setClientInfo(p);
      }

      @Override
      public String getClientInfo(String n) throws SQLException {
         return this.delegate.getClientInfo(n);
      }

      @Override
      public Properties getClientInfo() throws SQLException {
         return this.delegate.getClientInfo();
      }

      @Override
      public Array createArrayOf(String t, Object[] e) throws SQLException {
         return this.delegate.createArrayOf(t, e);
      }

      @Override
      public Struct createStruct(String t, Object[] a) throws SQLException {
         return this.delegate.createStruct(t, a);
      }

      @Override
      public void setSchema(String s) throws SQLException {
         this.delegate.setSchema(s);
      }

      @Override
      public String getSchema() throws SQLException {
         return this.delegate.getSchema();
      }

      @Override
      public void abort(Executor e) throws SQLException {
         this.delegate.abort(e);
      }

      @Override
      public void setNetworkTimeout(Executor e, int ms) throws SQLException {
         this.delegate.setNetworkTimeout(e, ms);
      }

      @Override
      public int getNetworkTimeout() throws SQLException {
         return this.delegate.getNetworkTimeout();
      }

      @Override
      public <T> T unwrap(Class<T> i) throws SQLException {
         return this.delegate.unwrap(i);
      }

      @Override
      public boolean isWrapperFor(Class<?> i) throws SQLException {
         return this.delegate.isWrapperFor(i);
      }
   }

   private static final class SingleConnectionDataSource implements DataSource {
      private final Connection conn;
      private final ReentrantLock lock = new ReentrantLock();

      private SingleConnectionDataSource(Connection conn) {
         this.conn = conn;
      }

      @Override
      public Connection getConnection() {
         this.lock.lock();
         return new SqliteBackendV2.NonClosingConnection(this.conn, this.lock);
      }

      @Override
      public Connection getConnection(String u, String p) {
         return this.getConnection();
      }

      @Override
      public PrintWriter getLogWriter() {
         return null;
      }

      @Override
      public void setLogWriter(PrintWriter out) {
      }

      @Override
      public void setLoginTimeout(int seconds) {
      }

      @Override
      public int getLoginTimeout() {
         return 0;
      }

      @Override
      public Logger getParentLogger() {
         return Logger.getGlobal();
      }

      @Override
      public <T> T unwrap(Class<T> iface) {
         return null;
      }

      @Override
      public boolean isWrapperFor(Class<?> iface) {
         return false;
      }
   }
}

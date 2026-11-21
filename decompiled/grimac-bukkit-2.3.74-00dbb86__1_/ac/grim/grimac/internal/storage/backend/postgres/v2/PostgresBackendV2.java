package ac.grim.grimac.internal.storage.backend.postgres.v2;

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
import ac.grim.grimac.internal.storage.backend.postgres.PostgresBackendConfig;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlCounterAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlEntityAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlEventStreamAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlKeyValueScopedAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlServerOwnershipAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.dialect.PostgresDialect;
import ac.grim.grimac.internal.storage.backend.sql.v2.dialect.SqlDialect;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.zaxxer.hikari.HikariConfig;
import ac.grim.grimac.shaded.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Logger;

@ApiStatus.Internal
public final class PostgresBackendV2 implements BackendV2 {
   private static final int DEFAULT_MAX_POOL_SIZE = 5;
   private static final String POOL_NAME = "grim-postgres-v2";
   @NotNull
   private final PostgresBackendConfig config;
   @NotNull
   private final SqlDialect dialect = new PostgresDialect();
   private Logger logger;
   private HikariDataSource ds;
   private SqlEntityAdapter entityAdapter;
   private SqlServerOwnershipAdapter ownershipAdapter;
   private SqlEventStreamAdapter eventStreamAdapter;
   private SqlKeyValueScopedAdapter kvScopedAdapter;
   private SqlCounterAdapter counterAdapter;

   public PostgresBackendV2(@NotNull PostgresBackendConfig config) {
      this.config = config;
   }

   @NotNull
   @Override
   public String id() {
      return "postgres-v2";
   }

   @NotNull
   @Override
   public ApiVersion apiVersion() {
      return ApiVersion.CURRENT;
   }

   @Override
   public int writerThreads(@NotNull Category<?> category) {
      return this.config.writerThreadsFor(category.id());
   }

   @NotNull
   @Override
   public EnumSet<Capability> capabilities() {
      return EnumSet.of(
         Capability.KIND_ENTITY,
         Capability.KIND_EVENT_STREAM,
         Capability.KIND_KV_SCOPED,
         Capability.KIND_COUNTER,
         Capability.ATOMIC_UPSERT,
         Capability.MULTI_WRITER
      );
   }

   @Override
   public void init(@NotNull BackendContext ctx) throws BackendException {
      this.logger = ctx.logger();

      try {
         Class.forName("org.postgresql.Driver");
         Class.forName("ac.grim.grimac.shaded.zaxxer.hikari.HikariDataSource");
      } catch (ClassNotFoundException var10) {
         throw new BackendException(
            "postgresql JDBC driver or HikariCP not on the classpath — shade them into the plugin jar or drop into server/plugins (missing: "
               + var10.getMessage()
               + ")",
            var10
         );
      }

      String jdbcUrl = withConnectTimeout(this.config.jdbcUrl(), 5);
      HikariConfig hc = new HikariConfig();
      hc.setJdbcUrl(jdbcUrl);
      hc.setUsername(this.config.user());
      if (this.config.password() != null) {
         hc.setPassword(this.config.password());
      }

      hc.setMaximumPoolSize(5);
      hc.setPoolName("grim-postgres-v2");
      hc.setConnectionTimeout(5000L);
      hc.setValidationTimeout(3000L);
      hc.setIdleTimeout(600000L);

      HikariDataSource pool;
      try {
         pool = new HikariDataSource(hc);
      } catch (RuntimeException var9) {
         throw new BackendException("failed to initialise Hikari pool for postgres", var9);
      }

      this.ds = pool;

      try (Connection probe = this.ds.getConnection()) {
         if (!probe.isValid(5)) {
            throw new BackendException("postgres connection probe returned invalid", null);
         }
      } catch (SQLException var12) {
         this.ds.close();
         throw new BackendException("postgres connection probe failed", var12);
      }

      this.entityAdapter = new SqlEntityAdapter(this.ds, this.dialect, this.logger);
      this.ownershipAdapter = new SqlServerOwnershipAdapter(this.ds, this.dialect, this.logger);
      this.eventStreamAdapter = new SqlEventStreamAdapter(this.ds, this.dialect, this.logger);
      this.kvScopedAdapter = new SqlKeyValueScopedAdapter(this.ds, this.dialect, this.logger);
      this.counterAdapter = new SqlCounterAdapter(this.ds, this.dialect, this.logger);
   }

   @Override
   public void flush() {
   }

   @Override
   public void close() throws BackendException {
      if (this.ds != null) {
         try {
            this.ds.close();
         } catch (RuntimeException var2) {
            throw new BackendException("failed to close Hikari pool", var2);
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
      return type.isInstance(this.ds) ? Optional.of((X)this.ds) : Optional.empty();
   }

   @NotNull
   static String withConnectTimeout(@NotNull String url, int seconds) {
      if (url.contains("connectTimeout=")) {
         return url;
      } else {
         char sep = (char)(url.indexOf(63) >= 0 ? 38 : 63);
         return url + sep + "connectTimeout=" + seconds;
      }
   }
}

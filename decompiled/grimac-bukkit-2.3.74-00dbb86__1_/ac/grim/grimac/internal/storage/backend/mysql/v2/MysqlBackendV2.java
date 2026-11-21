package ac.grim.grimac.internal.storage.backend.mysql.v2;

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
import ac.grim.grimac.internal.storage.backend.mysql.MysqlBackendConfig;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlCounterAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlEntityAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlEventStreamAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlKeyValueScopedAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.SqlServerOwnershipAdapter;
import ac.grim.grimac.internal.storage.backend.sql.v2.dialect.MysqlDialect;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.zaxxer.hikari.HikariConfig;
import ac.grim.grimac.shaded.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.Optional;
import java.util.logging.Logger;
import javax.sql.DataSource;

@ApiStatus.Internal
public final class MysqlBackendV2 implements BackendV2 {
   private static final int DEFAULT_MAX_POOL_SIZE = 10;
   @NotNull
   private final MysqlBackendConfig config;
   @NotNull
   private final MysqlDialect dialect = new MysqlDialect();
   private Logger logger;
   private HikariDataSource ds;
   private SqlEntityAdapter entityAdapter;
   private SqlServerOwnershipAdapter ownershipAdapter;
   private SqlEventStreamAdapter eventStreamAdapter;
   private SqlKeyValueScopedAdapter kvScopedAdapter;
   private SqlCounterAdapter counterAdapter;

   public MysqlBackendV2(@NotNull MysqlBackendConfig config) {
      this.config = config;
   }

   @NotNull
   @Override
   public String id() {
      return "mysql-v2";
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
      return EnumSet.of(Capability.KIND_ENTITY, Capability.KIND_EVENT_STREAM, Capability.KIND_KV_SCOPED, Capability.KIND_COUNTER, Capability.ATOMIC_UPSERT);
   }

   @Override
   public void init(@NotNull BackendContext ctx) throws BackendException {
      this.logger = ctx.logger();

      try {
         Class.forName("com.mysql.cj.jdbc.Driver");
         Class.forName("ac.grim.grimac.shaded.zaxxer.hikari.HikariDataSource");
      } catch (ClassNotFoundException var8) {
         throw new BackendException("MySQL JDBC driver or HikariCP missing: " + var8.getMessage(), var8);
      }

      HikariConfig hc = new HikariConfig();
      hc.setJdbcUrl(this.config.jdbcUrl());
      hc.setUsername(this.config.user());
      if (this.config.password() != null) {
         hc.setPassword(this.config.password());
      }

      hc.setMaximumPoolSize(10);
      hc.setPoolName("grim-mysql-v2");
      hc.setConnectionTimeout(5000L);

      try {
         this.ds = new HikariDataSource(hc);
      } catch (RuntimeException var7) {
         throw new BackendException("failed to init MySQL pool", var7);
      }

      try (Connection probe = this.ds.getConnection()) {
         if (!probe.isValid(5)) {
            throw new BackendException("MySQL probe invalid", null);
         }
      } catch (SQLException var10) {
         this.ds.close();
         throw new BackendException("MySQL probe failed", var10);
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
            throw new BackendException("MySQL close failed", var2);
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
      return type.isAssignableFrom(DataSource.class) && this.ds != null ? Optional.of((X)this.ds) : Optional.empty();
   }
}

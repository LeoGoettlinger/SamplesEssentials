package ac.grim.grimac.internal.storage.backend.mysql;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Map;

@ApiStatus.Internal
public record MysqlBackendConfig(
   @NotNull String host,
   int port,
   @NotNull String database,
   @NotNull String user,
   @Nullable String password,
   @NotNull String extraJdbcParams,
   int batchFlushCap,
   int writerThreadsDefault,
   @NotNull Map<String, Integer> writerThreadsPerCategory,
   @NotNull TableNames tableNames
) implements BackendConfig {
   public MysqlBackendConfig(
      @NotNull String host,
      int port,
      @NotNull String database,
      @NotNull String user,
      @Nullable String password,
      @NotNull String extraJdbcParams,
      int batchFlushCap,
      int writerThreadsDefault,
      @NotNull Map<String, Integer> writerThreadsPerCategory,
      @NotNull TableNames tableNames
   ) {
      if (batchFlushCap <= 0) {
         batchFlushCap = 256;
      }

      if (writerThreadsDefault <= 0) {
         writerThreadsDefault = 1;
      }

      if (writerThreadsPerCategory == null) {
         writerThreadsPerCategory = Map.of();
      }

      if (tableNames == null) {
         tableNames = TableNames.DEFAULTS;
      }

      this.host = host;
      this.port = port;
      this.database = database;
      this.user = user;
      this.password = password;
      this.extraJdbcParams = extraJdbcParams;
      this.batchFlushCap = batchFlushCap;
      this.writerThreadsDefault = writerThreadsDefault;
      this.writerThreadsPerCategory = writerThreadsPerCategory;
      this.tableNames = tableNames;
   }

   public MysqlBackendConfig(
      @NotNull String host,
      int port,
      @NotNull String database,
      @NotNull String user,
      @Nullable String password,
      @NotNull String extraJdbcParams,
      int batchFlushCap,
      @NotNull TableNames tableNames
   ) {
      this(host, port, database, user, password, extraJdbcParams, batchFlushCap, 1, Map.of(), tableNames);
   }

   public int writerThreadsFor(@NotNull String categoryId) {
      return this.writerThreadsPerCategory.getOrDefault(categoryId, this.writerThreadsDefault);
   }

   public String jdbcUrl() {
      StringBuilder sb = new StringBuilder("jdbc:mysql://")
         .append(this.host)
         .append(':')
         .append(this.port)
         .append('/')
         .append(this.database)
         .append("?useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true");
      if (!this.extraJdbcParams.isEmpty()) {
         sb.append('&').append(this.extraJdbcParams);
      }

      return sb.toString();
   }
}

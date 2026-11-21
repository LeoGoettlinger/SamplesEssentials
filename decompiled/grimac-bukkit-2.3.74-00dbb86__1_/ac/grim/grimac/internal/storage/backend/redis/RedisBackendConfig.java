package ac.grim.grimac.internal.storage.backend.redis;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Map;

@ApiStatus.Internal
public record RedisBackendConfig(
   @NotNull String host,
   int port,
   int database,
   @Nullable String user,
   @Nullable String password,
   @NotNull String keyPrefix,
   int timeoutMs,
   int batchFlushCap,
   boolean warnOnHistory,
   int writerThreadsDefault,
   @NotNull Map<String, Integer> writerThreadsPerCategory,
   @NotNull TableNames tableNames
) implements BackendConfig {
   public RedisBackendConfig(
      @NotNull String host,
      int port,
      int database,
      @Nullable String user,
      @Nullable String password,
      @NotNull String keyPrefix,
      int timeoutMs,
      int batchFlushCap,
      boolean warnOnHistory,
      int writerThreadsDefault,
      @NotNull Map<String, Integer> writerThreadsPerCategory,
      @NotNull TableNames tableNames
   ) {
      if (batchFlushCap <= 0) {
         batchFlushCap = 256;
      }

      if (timeoutMs <= 0) {
         timeoutMs = 2000;
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

      if (keyPrefix == null) {
         keyPrefix = "";
      }

      this.host = host;
      this.port = port;
      this.database = database;
      this.user = user;
      this.password = password;
      this.keyPrefix = keyPrefix;
      this.timeoutMs = timeoutMs;
      this.batchFlushCap = batchFlushCap;
      this.warnOnHistory = warnOnHistory;
      this.writerThreadsDefault = writerThreadsDefault;
      this.writerThreadsPerCategory = writerThreadsPerCategory;
      this.tableNames = tableNames;
   }

   public RedisBackendConfig(
      @NotNull String host,
      int port,
      int database,
      @Nullable String user,
      @Nullable String password,
      @NotNull String keyPrefix,
      int timeoutMs,
      int batchFlushCap,
      boolean warnOnHistory,
      @NotNull TableNames tableNames
   ) {
      this(host, port, database, user, password, keyPrefix, timeoutMs, batchFlushCap, warnOnHistory, 1, Map.of(), tableNames);
   }

   public int writerThreadsFor(@NotNull String categoryId) {
      return this.writerThreadsPerCategory.getOrDefault(categoryId, this.writerThreadsDefault);
   }
}

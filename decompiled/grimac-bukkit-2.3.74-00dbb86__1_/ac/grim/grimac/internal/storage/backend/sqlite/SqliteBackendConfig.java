package ac.grim.grimac.internal.storage.backend.sqlite;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public record SqliteBackendConfig(
   @NotNull String path,
   @NotNull String journalMode,
   @NotNull String synchronousMode,
   int busyTimeoutMs,
   int cachePages,
   int batchFlushCap,
   @NotNull TableNames tableNames
) implements BackendConfig {
   public SqliteBackendConfig(
      @NotNull String path,
      @NotNull String journalMode,
      @NotNull String synchronousMode,
      int busyTimeoutMs,
      int cachePages,
      int batchFlushCap,
      @NotNull TableNames tableNames
   ) {
      if (batchFlushCap <= 0) {
         batchFlushCap = 256;
      }

      if (tableNames == null) {
         tableNames = TableNames.DEFAULTS;
      }

      this.path = path;
      this.journalMode = journalMode;
      this.synchronousMode = synchronousMode;
      this.busyTimeoutMs = busyTimeoutMs;
      this.cachePages = cachePages;
      this.batchFlushCap = batchFlushCap;
      this.tableNames = tableNames;
   }

   public static SqliteBackendConfig defaults(String path) {
      return new SqliteBackendConfig(path, "WAL", "NORMAL", 5000, 10000, 256, TableNames.DEFAULTS);
   }
}

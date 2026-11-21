package ac.grim.grimac.internal.storage.backend.mongo;

import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Map;

@ApiStatus.Internal
public record MongoBackendConfig(
   @NotNull String connectionString,
   @NotNull String database,
   int batchFlushCap,
   int writerThreadsDefault,
   @NotNull Map<String, Integer> writerThreadsPerCategory,
   @NotNull TableNames tableNames
) implements BackendConfig {
   public MongoBackendConfig(
      @NotNull String connectionString,
      @NotNull String database,
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

      this.connectionString = connectionString;
      this.database = database;
      this.batchFlushCap = batchFlushCap;
      this.writerThreadsDefault = writerThreadsDefault;
      this.writerThreadsPerCategory = writerThreadsPerCategory;
      this.tableNames = tableNames;
   }

   public MongoBackendConfig(@NotNull String connectionString, @NotNull String database, int batchFlushCap, @NotNull TableNames tableNames) {
      this(connectionString, database, batchFlushCap, 1, Map.of(), tableNames);
   }

   public int writerThreadsFor(@NotNull String categoryId) {
      return this.writerThreadsPerCategory.getOrDefault(categoryId, this.writerThreadsDefault);
   }
}

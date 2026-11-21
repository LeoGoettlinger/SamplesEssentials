package ac.grim.grimac.internal.storage.backend.sqlite;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class SqliteBackendProvider implements BackendProvider {
   @NotNull
   @Override
   public String id() {
      return "sqlite";
   }

   @NotNull
   @Override
   public Class<? extends BackendConfig> configType() {
      return SqliteBackendConfig.class;
   }

   @NotNull
   @Override
   public BackendConfig readConfig(@NotNull BackendConfigSource src) {
      return new SqliteBackendConfig(
         src.getString("path", "data/history.v1.db"),
         src.getString("journal-mode", "WAL"),
         src.getString("synchronous-mode", "NORMAL"),
         src.getInt("busy-timeout-ms", 5000),
         src.getInt("cache-pages", 10000),
         src.getInt("batch-flush-cap", 256),
         TableNames.readFrom(src)
      );
   }

   @NotNull
   @Override
   public Backend create(@NotNull BackendConfig config) {
      if (config instanceof SqliteBackendConfig c) {
         return new SqliteBackend(c);
      } else {
         throw new IllegalArgumentException(
            "SqliteBackendProvider requires SqliteBackendConfig, got " + (config == null ? "null" : config.getClass().getName())
         );
      }
   }
}

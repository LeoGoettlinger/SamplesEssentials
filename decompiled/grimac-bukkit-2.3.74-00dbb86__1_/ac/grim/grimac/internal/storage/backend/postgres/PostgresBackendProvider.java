package ac.grim.grimac.internal.storage.backend.postgres;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.LinkedHashMap;
import java.util.Map;

@ApiStatus.Internal
public final class PostgresBackendProvider implements BackendProvider {
   @NotNull
   @Override
   public String id() {
      return "postgres";
   }

   @NotNull
   @Override
   public Class<? extends BackendConfig> configType() {
      return PostgresBackendConfig.class;
   }

   @NotNull
   @Override
   public BackendConfig readConfig(@NotNull BackendConfigSource src) {
      String pw = src.getString("password", "");
      int wtDefault = src.getInt("writer-threads.default", 1);
      Map<String, Integer> wtPerCat = new LinkedHashMap<>();

      for (String cat : new String[]{"violation", "session", "player-identity", "setting", "blob"}) {
         int v = src.getInt("writer-threads." + cat, -1);
         if (v > 0) {
            wtPerCat.put(cat, v);
         }
      }

      return new PostgresBackendConfig(
         src.getString("host", "localhost"),
         src.getInt("port", 5432),
         src.getString("database", "grim"),
         src.getString("user", "postgres"),
         pw.isEmpty() ? null : pw,
         src.getString("extra-jdbc-params", ""),
         src.getInt("batch-flush-cap", 256),
         wtDefault,
         wtPerCat,
         TableNames.readFrom(src)
      );
   }

   @NotNull
   @Override
   public Backend create(@NotNull BackendConfig config) {
      if (config instanceof PostgresBackendConfig c) {
         return new PostgresBackend(c);
      } else {
         throw new IllegalArgumentException(
            "PostgresBackendProvider requires PostgresBackendConfig, got " + (config == null ? "null" : config.getClass().getName())
         );
      }
   }
}

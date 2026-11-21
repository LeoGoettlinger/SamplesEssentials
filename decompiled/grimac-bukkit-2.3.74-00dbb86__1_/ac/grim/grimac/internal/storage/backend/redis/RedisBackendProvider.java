package ac.grim.grimac.internal.storage.backend.redis;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class RedisBackendProvider implements BackendProvider {
   @NotNull
   @Override
   public String id() {
      return "redis";
   }

   @NotNull
   @Override
   public Class<? extends BackendConfig> configType() {
      return RedisBackendConfig.class;
   }

   @NotNull
   @Override
   public BackendConfig readConfig(@NotNull BackendConfigSource src) {
      String user = src.getString("user", "");
      String pw = src.getString("password", "");
      return new RedisBackendConfig(
         src.getString("host", "localhost"),
         src.getInt("port", 6379),
         src.getInt("database", 0),
         user.isEmpty() ? null : user,
         pw.isEmpty() ? null : pw,
         src.getString("key-prefix", ""),
         src.getInt("timeout-ms", 2000),
         src.getInt("batch-flush-cap", 256),
         src.getBoolean("warn-on-history", true),
         TableNames.readFrom(src)
      );
   }

   @NotNull
   @Override
   public Backend create(@NotNull BackendConfig config) {
      if (config instanceof RedisBackendConfig c) {
         return new RedisBackend(c);
      } else {
         throw new IllegalArgumentException("RedisBackendProvider requires RedisBackendConfig, got " + (config == null ? "null" : config.getClass().getName()));
      }
   }
}

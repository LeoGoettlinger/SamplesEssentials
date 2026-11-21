package ac.grim.grimac.internal.storage.backend.mongo;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.config.TableNames;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class MongoBackendProvider implements BackendProvider {
   @NotNull
   @Override
   public String id() {
      return "mongo";
   }

   @NotNull
   @Override
   public Class<? extends BackendConfig> configType() {
      return MongoBackendConfig.class;
   }

   @NotNull
   @Override
   public BackendConfig readConfig(@NotNull BackendConfigSource src) {
      return new MongoBackendConfig(
         src.getString("connection-string", "mongodb://localhost:27017"),
         src.getString("database", "grim"),
         src.getInt("batch-flush-cap", 256),
         TableNames.readFrom(src)
      );
   }

   @NotNull
   @Override
   public Backend create(@NotNull BackendConfig config) {
      if (config instanceof MongoBackendConfig c) {
         return new MongoBackend(c);
      } else {
         throw new IllegalArgumentException("MongoBackendProvider requires MongoBackendConfig, got " + (config == null ? "null" : config.getClass().getName()));
      }
   }
}

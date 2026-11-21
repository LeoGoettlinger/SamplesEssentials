package ac.grim.grimac.internal.storage.backend.memory;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.backend.BackendConfig;
import ac.grim.grimac.api.storage.backend.BackendConfigSource;
import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Internal
public final class InMemoryBackendProvider implements BackendProvider {
   @NotNull
   @Override
   public String id() {
      return "memory";
   }

   @NotNull
   @Override
   public Class<? extends BackendConfig> configType() {
      return InMemoryBackend.Config.class;
   }

   @NotNull
   @Override
   public BackendConfig readConfig(@NotNull BackendConfigSource src) {
      return new InMemoryBackend.Config();
   }

   @NotNull
   @Override
   public Backend create(@NotNull BackendConfig config) {
      return new InMemoryBackend();
   }
}

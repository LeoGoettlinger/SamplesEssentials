package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface BackendProvider {
   @NotNull
   String id();

   @NotNull
   Class<? extends BackendConfig> configType();

   @NotNull
   BackendConfig readConfig(@NotNull BackendConfigSource var1);

   @NotNull
   Backend create(@NotNull BackendConfig var1);
}

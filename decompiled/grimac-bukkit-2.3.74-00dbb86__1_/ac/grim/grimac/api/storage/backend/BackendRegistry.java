package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Set;

@ApiStatus.Experimental
public interface BackendRegistry {
   void register(@NotNull BackendProvider var1);

   @Nullable
   BackendProvider lookup(@NotNull String var1);

   @NotNull
   Set<String> registeredIds();
}

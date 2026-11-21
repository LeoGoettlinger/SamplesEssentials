package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;

@ApiStatus.Experimental
public interface BackendConfigSource {
   @NotNull
   String getString(@NotNull String var1, @NotNull String var2);

   int getInt(@NotNull String var1, int var2);

   long getLong(@NotNull String var1, long var2);

   boolean getBoolean(@NotNull String var1, boolean var2);

   @NotNull
   List<String> getStringList(@NotNull String var1, @NotNull List<String> var2);
}

package ac.grim.grimac.api.storage.verbose;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface VerboseFormatter {
   int version();

   void render(@NotNull VerboseBuf var1, @NotNull VerboseRenderContext var2, @NotNull VerboseSink var3);
}

package ac.grim.grimac.api.storage.verbose;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public record VerboseRenderContext(int clientVersionPvn, @Nullable String serverVersion) {
}

package ac.grim.grimac.api.storage.instance;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public record ServerOwnershipMetadata(
   @Nullable String serverName, @Nullable String hostname, @Nullable String grimVersion, @Nullable String serverVersionString
) {
}

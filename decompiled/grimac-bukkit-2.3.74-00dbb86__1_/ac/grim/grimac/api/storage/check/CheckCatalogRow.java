package ac.grim.grimac.api.storage.check;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public record CheckCatalogRow(
   int checkId, String stableKey, @Nullable String display, @Nullable String description, @Nullable String introducedVersion, long introducedAt
) {
}

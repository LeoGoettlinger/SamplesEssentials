package ac.grim.grimac.api.storage.check;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record CheckCatalogRepairResult(int mappingsApplied, long violationsUpdated, long catalogVersionsUpdated) {
}

package ac.grim.grimac.api.storage.search;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public record Scored<R>(@NotNull R value, double normalizedScore, double rawScore) {
}

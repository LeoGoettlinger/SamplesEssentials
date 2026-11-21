package ac.grim.grimac.api.storage.kind;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public record KeyValueRecord<S, V>(@NotNull S scope, @NotNull String scopeKey, @NotNull String key, @NotNull V value, long updatedEpochMs) {
}

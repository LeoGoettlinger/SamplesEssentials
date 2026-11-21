package ac.grim.grimac.api.storage.admin;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public record CollectionStats(long documentCount, long storageBytes, long dataBytes, long indexBytes, double compressionRatio) {
}

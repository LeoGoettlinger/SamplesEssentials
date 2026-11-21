package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;

@ApiStatus.Experimental
public record BlobRef(String backendId, String key, long sizeBytes, @Nullable String contentType) {
   public BlobRef(String backendId, String key, long sizeBytes, @Nullable String contentType) {
      if (backendId == null) {
         throw new IllegalArgumentException("backendId");
      } else if (key == null) {
         throw new IllegalArgumentException("key");
      } else {
         this.backendId = backendId;
         this.key = key;
         this.sizeBytes = sizeBytes;
         this.contentType = contentType;
      }
   }
}

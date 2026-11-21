package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@ApiStatus.Experimental
public record SessionBlobRecord(
   UUID sessionId,
   UUID playerUuid,
   BlobRef blobRef,
   @Nullable String kind,
   @Nullable String codec,
   long startOffsetMs,
   long durationMs,
   @Nullable String label,
   @Nullable String metadata,
   boolean truncated
) {
   public SessionBlobRecord(
      UUID sessionId,
      UUID playerUuid,
      BlobRef blobRef,
      @Nullable String kind,
      @Nullable String codec,
      long startOffsetMs,
      long durationMs,
      @Nullable String label,
      @Nullable String metadata,
      boolean truncated
   ) {
      if (sessionId == null) {
         throw new IllegalArgumentException("sessionId");
      } else if (playerUuid == null) {
         throw new IllegalArgumentException("playerUuid");
      } else if (blobRef == null) {
         throw new IllegalArgumentException("blobRef");
      } else {
         this.sessionId = sessionId;
         this.playerUuid = playerUuid;
         this.blobRef = blobRef;
         this.kind = kind;
         this.codec = codec;
         this.startOffsetMs = startOffsetMs;
         this.durationMs = durationMs;
         this.label = label;
         this.metadata = metadata;
         this.truncated = truncated;
      }
   }
}

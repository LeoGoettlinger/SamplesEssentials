package ac.grim.grimac.api.storage.event;

import ac.grim.grimac.api.storage.model.BlobRef;
import ac.grim.grimac.api.storage.model.SessionBlobRecord;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@ApiStatus.Experimental
public final class BlobEvent {
   private BlobRef blobRef;
   private UUID sessionId;
   private UUID playerUuid;
   @Nullable
   private String kind;
   @Nullable
   private String codec;
   private long startOffsetMs;
   private long durationMs;
   @Nullable
   private String label;
   @Nullable
   private String metadata;
   private boolean truncated;

   @NotNull
   public BlobRef blobRef() {
      return this.blobRef;
   }

   @NotNull
   public BlobEvent blobRef(@NotNull BlobRef v) {
      this.blobRef = v;
      return this;
   }

   @NotNull
   public UUID sessionId() {
      return this.sessionId;
   }

   @NotNull
   public BlobEvent sessionId(@NotNull UUID v) {
      this.sessionId = v;
      return this;
   }

   @NotNull
   public UUID playerUuid() {
      return this.playerUuid;
   }

   @NotNull
   public BlobEvent playerUuid(@NotNull UUID v) {
      this.playerUuid = v;
      return this;
   }

   @Nullable
   public String kind() {
      return this.kind;
   }

   @NotNull
   public BlobEvent kind(@Nullable String v) {
      this.kind = v;
      return this;
   }

   @Nullable
   public String codec() {
      return this.codec;
   }

   @NotNull
   public BlobEvent codec(@Nullable String v) {
      this.codec = v;
      return this;
   }

   public long startOffsetMs() {
      return this.startOffsetMs;
   }

   @NotNull
   public BlobEvent startOffsetMs(long v) {
      this.startOffsetMs = v;
      return this;
   }

   public long durationMs() {
      return this.durationMs;
   }

   @NotNull
   public BlobEvent durationMs(long v) {
      this.durationMs = v;
      return this;
   }

   @Nullable
   public String label() {
      return this.label;
   }

   @NotNull
   public BlobEvent label(@Nullable String v) {
      this.label = v;
      return this;
   }

   @Nullable
   public String metadata() {
      return this.metadata;
   }

   @NotNull
   public BlobEvent metadata(@Nullable String v) {
      this.metadata = v;
      return this;
   }

   public boolean truncated() {
      return this.truncated;
   }

   @NotNull
   public BlobEvent truncated(boolean v) {
      this.truncated = v;
      return this;
   }

   @NotNull
   public SessionBlobRecord toRecord() {
      return new SessionBlobRecord(
         this.sessionId, this.playerUuid, this.blobRef, this.kind, this.codec, this.startOffsetMs, this.durationMs, this.label, this.metadata, this.truncated
      );
   }

   public void reset() {
      this.blobRef = null;
      this.sessionId = null;
      this.playerUuid = null;
      this.kind = null;
      this.codec = null;
      this.startOffsetMs = 0L;
      this.durationMs = 0L;
      this.label = null;
      this.metadata = null;
      this.truncated = false;
   }
}

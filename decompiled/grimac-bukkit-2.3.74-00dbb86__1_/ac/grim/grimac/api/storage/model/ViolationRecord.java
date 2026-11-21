package ac.grim.grimac.api.storage.model;

import ac.grim.grimac.api.storage.codec.Id;
import ac.grim.grimac.api.storage.codec.Name;
import ac.grim.grimac.api.storage.codec.Nullable;
import ac.grim.grimac.api.storage.codec.Partition;
import ac.grim.grimac.api.storage.codec.Persistent;
import ac.grim.grimac.api.storage.codec.Timestamp;
import ac.grim.grimac.api.storage.codec.Value;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Persistent
@ApiStatus.Experimental
public record ViolationRecord(
   @Id UUID id,
   @Partition UUID sessionId,
   @Partition UUID playerUuid,
   @Partition int checkId,
   @Value double vl,
   @Timestamp @Name("occurred_at") long occurredEpochMs,
   @Value @Name("verbose") @Nullable byte[] verboseData,
   @Value @Name("verbose_format") VerboseFormat verboseFormat
) {
   public ViolationRecord(
      UUID id, UUID sessionId, UUID playerUuid, int checkId, double vl, long occurredEpochMs, byte[] verboseData, VerboseFormat verboseFormat
   ) {
      if (id == null) {
         throw new IllegalArgumentException("id");
      } else if (sessionId == null) {
         throw new IllegalArgumentException("sessionId");
      } else if (playerUuid == null) {
         throw new IllegalArgumentException("playerUuid");
      } else {
         if (verboseFormat == null) {
            verboseFormat = VerboseFormat.TEXT;
         }

         this.id = id;
         this.sessionId = sessionId;
         this.playerUuid = playerUuid;
         this.checkId = checkId;
         this.vl = vl;
         this.occurredEpochMs = occurredEpochMs;
         this.verboseData = verboseData;
         this.verboseFormat = verboseFormat;
      }
   }

   public ViolationRecord(UUID id, UUID sessionId, UUID playerUuid, int checkId, double vl, long occurredEpochMs, byte[] verboseData) {
      this(id, sessionId, playerUuid, checkId, vl, occurredEpochMs, verboseData, VerboseFormat.TEXT);
   }

   public ViolationRecord(
      UUID id,
      UUID sessionId,
      UUID playerUuid,
      int checkId,
      double vl,
      long occurredEpochMs,
      @ac.grim.grimac.shaded.jetbrains.annotations.Nullable String verbose,
      VerboseFormat verboseFormat
   ) {
      this(id, sessionId, playerUuid, checkId, vl, occurredEpochMs, verbose == null ? null : verbose.getBytes(StandardCharsets.UTF_8), verboseFormat);
   }

   @ac.grim.grimac.shaded.jetbrains.annotations.Nullable
   public String verbose() {
      return this.verboseData == null ? null : new String(this.verboseData, StandardCharsets.UTF_8);
   }
}

package ac.grim.grimac.api.storage.history;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.UUID;

@ApiStatus.Experimental
public record SessionDetail(
   @NotNull UUID sessionId,
   @NotNull UUID playerUuid,
   int sessionOrdinal,
   long startedEpochMs,
   long lastActivityEpochMs,
   @Nullable String grimVersion,
   @Nullable String serverName,
   int clientVersion,
   @Nullable String clientBrand,
   long bucketSizeMs,
   int uniqueCheckCount,
   @NotNull List<CheckBucket> buckets,
   @NotNull List<ViolationEntry> violations
) {
   public SessionDetail(
      @NotNull UUID sessionId,
      @NotNull UUID playerUuid,
      int sessionOrdinal,
      long startedEpochMs,
      long lastActivityEpochMs,
      @Nullable String grimVersion,
      @Nullable String serverName,
      int clientVersion,
      @Nullable String clientBrand,
      long bucketSizeMs,
      int uniqueCheckCount,
      @NotNull List<CheckBucket> buckets,
      @NotNull List<ViolationEntry> violations
   ) {
      buckets = buckets == null ? List.of() : List.copyOf(buckets);
      violations = violations == null ? List.of() : List.copyOf(violations);
      this.sessionId = sessionId;
      this.playerUuid = playerUuid;
      this.sessionOrdinal = sessionOrdinal;
      this.startedEpochMs = startedEpochMs;
      this.lastActivityEpochMs = lastActivityEpochMs;
      this.grimVersion = grimVersion;
      this.serverName = serverName;
      this.clientVersion = clientVersion;
      this.clientBrand = clientBrand;
      this.bucketSizeMs = bucketSizeMs;
      this.uniqueCheckCount = uniqueCheckCount;
      this.buckets = buckets;
      this.violations = violations;
   }

   public long durationMs() {
      return Math.max(0L, this.lastActivityEpochMs - this.startedEpochMs);
   }
}

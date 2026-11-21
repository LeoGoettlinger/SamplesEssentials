package ac.grim.grimac.api.storage.history;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@ApiStatus.Experimental
public record SessionSummary(
   @NotNull UUID sessionId,
   @NotNull UUID playerUuid,
   int sessionOrdinal,
   long startedEpochMs,
   long lastActivityEpochMs,
   long closedAtEpochMs,
   @Nullable String grimVersion,
   @Nullable String serverName,
   int clientVersion,
   @Nullable String clientBrand,
   long violationCount,
   int uniqueCheckCount
) {
   public long durationMs() {
      return Math.max(0L, this.lastActivityEpochMs - this.startedEpochMs);
   }

   public boolean isClosed() {
      return this.closedAtEpochMs != 0L;
   }

   public boolean endedUnexpectedly() {
      return this.isClosed() && this.closedAtEpochMs == this.lastActivityEpochMs;
   }
}

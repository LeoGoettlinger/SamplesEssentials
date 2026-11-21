package ac.grim.grimac.api.storage.instance;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@ApiStatus.Experimental
public record ServerOwnershipSnapshot(
   @NotNull UUID persistentId,
   @NotNull UUID ownerStartupId,
   @NotNull UUID fence,
   long leaseExpiresAtEpochMs,
   long lastRenewedAtEpochMs,
   long closedAtEpochMs,
   @Nullable String closeReason,
   @Nullable String serverName,
   @Nullable String hostname,
   @Nullable String grimVersion,
   @Nullable String serverVersionString
) {
   public static final long OPEN = 0L;

   public boolean activeAt(long dbNowEpochMs) {
      return this.closedAtEpochMs == 0L && this.leaseExpiresAtEpochMs > dbNowEpochMs;
   }
}

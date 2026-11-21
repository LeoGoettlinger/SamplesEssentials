package ac.grim.grimac.api.storage.instance;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.UUID;

@ApiStatus.Experimental
public record OwnershipRenewResult(
   boolean renewed, @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long dbNowEpochMs, long leaseExpiresAtEpochMs
) {
   @NotNull
   public static OwnershipRenewResult renewed(
      @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long dbNowEpochMs, long leaseExpiresAtEpochMs
   ) {
      return new OwnershipRenewResult(true, persistentId, startupId, fence, dbNowEpochMs, leaseExpiresAtEpochMs);
   }

   @NotNull
   public static OwnershipRenewResult lost(@NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long dbNowEpochMs) {
      return new OwnershipRenewResult(false, persistentId, startupId, fence, dbNowEpochMs, dbNowEpochMs);
   }
}

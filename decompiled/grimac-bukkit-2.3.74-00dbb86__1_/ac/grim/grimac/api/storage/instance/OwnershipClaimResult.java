package ac.grim.grimac.api.storage.instance;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.UUID;

@ApiStatus.Experimental
public record OwnershipClaimResult(
   boolean claimed,
   @NotNull UUID persistentId,
   @NotNull UUID startupId,
   @NotNull UUID fence,
   long dbNowEpochMs,
   long leaseExpiresAtEpochMs,
   @Nullable ServerOwnershipSnapshot previousOwner,
   @Nullable ServerOwnershipSnapshot currentOwner
) {
   @NotNull
   public static OwnershipClaimResult claimed(
      @NotNull UUID persistentId,
      @NotNull UUID startupId,
      @NotNull UUID fence,
      long dbNowEpochMs,
      long leaseExpiresAtEpochMs,
      @Nullable ServerOwnershipSnapshot previousOwner
   ) {
      return new OwnershipClaimResult(true, persistentId, startupId, fence, dbNowEpochMs, leaseExpiresAtEpochMs, previousOwner, null);
   }

   @NotNull
   public static OwnershipClaimResult denied(
      @NotNull UUID persistentId, @NotNull UUID startupId, @NotNull UUID fence, long dbNowEpochMs, @NotNull ServerOwnershipSnapshot currentOwner
   ) {
      return new OwnershipClaimResult(false, persistentId, startupId, fence, dbNowEpochMs, currentOwner.leaseExpiresAtEpochMs(), null, currentOwner);
   }
}

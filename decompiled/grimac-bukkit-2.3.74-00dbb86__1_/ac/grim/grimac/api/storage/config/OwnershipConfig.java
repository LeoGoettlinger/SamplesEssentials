package ac.grim.grimac.api.storage.config;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public record OwnershipConfig(
   boolean enforcePersistentUuidOwnership,
   @NotNull DuplicatePersistentUuidAction duplicatePersistentUuidAction,
   long leaseTtlMs,
   long renewIntervalMs,
   long startupWaitMs,
   long safetyMarginMs,
   long staleStartupTtlMs,
   long recoverySweepIntervalMs,
   boolean cleanupOtherServers
) {
   public OwnershipConfig(
      boolean enforcePersistentUuidOwnership,
      @NotNull DuplicatePersistentUuidAction duplicatePersistentUuidAction,
      long leaseTtlMs,
      long renewIntervalMs,
      long startupWaitMs,
      long safetyMarginMs,
      long staleStartupTtlMs,
      long recoverySweepIntervalMs,
      boolean cleanupOtherServers
   ) {
      duplicatePersistentUuidAction = duplicatePersistentUuidAction == null ? DuplicatePersistentUuidAction.DISABLE_STORAGE : duplicatePersistentUuidAction;
      leaseTtlMs = Math.max(1L, leaseTtlMs);
      renewIntervalMs = Math.max(1L, renewIntervalMs);
      startupWaitMs = Math.max(0L, startupWaitMs);
      safetyMarginMs = Math.max(0L, safetyMarginMs);
      staleStartupTtlMs = Math.max(1L, staleStartupTtlMs);
      recoverySweepIntervalMs = Math.max(1L, recoverySweepIntervalMs);
      this.enforcePersistentUuidOwnership = enforcePersistentUuidOwnership;
      this.duplicatePersistentUuidAction = duplicatePersistentUuidAction;
      this.leaseTtlMs = leaseTtlMs;
      this.renewIntervalMs = renewIntervalMs;
      this.startupWaitMs = startupWaitMs;
      this.safetyMarginMs = safetyMarginMs;
      this.staleStartupTtlMs = staleStartupTtlMs;
      this.recoverySweepIntervalMs = recoverySweepIntervalMs;
      this.cleanupOtherServers = cleanupOtherServers;
   }

   @NotNull
   public static OwnershipConfig defaults() {
      return new OwnershipConfig(true, DuplicatePersistentUuidAction.DISABLE_STORAGE, 20000L, 10000L, 20000L, 5000L, 30000L, 10000L, true);
   }
}

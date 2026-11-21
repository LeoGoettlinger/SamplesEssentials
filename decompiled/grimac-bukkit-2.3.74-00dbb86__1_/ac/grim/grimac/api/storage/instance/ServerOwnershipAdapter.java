package ac.grim.grimac.api.storage.instance;

import ac.grim.grimac.api.storage.backend.BackendException;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Optional;
import java.util.UUID;

@ApiStatus.Experimental
public interface ServerOwnershipAdapter {
   void ensureStore(@NotNull StoreId var1) throws BackendException;

   long dbNowEpochMs() throws BackendException;

   @NotNull
   OwnershipClaimResult claimOwnership(
      @NotNull StoreId var1, @NotNull UUID var2, @NotNull UUID var3, @NotNull UUID var4, long var5, @NotNull ServerOwnershipMetadata var7
   ) throws BackendException;

   @NotNull
   OwnershipRenewResult renewOwnership(@NotNull StoreId var1, @NotNull UUID var2, @NotNull UUID var3, @NotNull UUID var4, long var5) throws BackendException;

   boolean closeOwnership(@NotNull StoreId var1, @NotNull UUID var2, @NotNull UUID var3, @NotNull UUID var4, @NotNull String var5) throws BackendException;

   @NotNull
   Optional<ServerOwnershipSnapshot> readOwnership(@NotNull StoreId var1, @NotNull UUID var2) throws BackendException;
}

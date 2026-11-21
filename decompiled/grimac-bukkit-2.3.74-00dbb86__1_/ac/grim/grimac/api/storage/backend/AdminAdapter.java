package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.api.storage.admin.CollectionStats;
import ac.grim.grimac.api.storage.admin.ExplainPlan;
import ac.grim.grimac.api.storage.admin.IndexStats;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.api.storage.search.SearchSpec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.time.Duration;

@ApiStatus.Experimental
public interface AdminAdapter {
   @NotNull
   ExplainPlan explain(@NotNull StoreId var1, @NotNull Operation<?> var2) throws BackendException;

   @NotNull
   ExplainPlan explainSearch(@NotNull StoreId var1, @NotNull SearchSpec var2) throws BackendException;

   void reindex(@NotNull StoreId var1) throws BackendException;

   void rebuildSearchIndex(@NotNull StoreId var1) throws BackendException;

   void setRetention(@NotNull StoreId var1, @NotNull Duration var2) throws BackendException;

   @NotNull
   IndexStats indexStats(@NotNull StoreId var1) throws BackendException;

   @NotNull
   CollectionStats collectionStats(@NotNull StoreId var1) throws BackendException;
}

package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.codec.EncodeShape;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.api.storage.search.SearchResult;
import ac.grim.grimac.api.storage.search.SearchSpec;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.EnumSet;

@ApiStatus.Experimental
public interface SearchAdapter {
   @NotNull
   EnumSet<Capability> searchCapabilities();

   void ensureSearchIndex(@NotNull StoreId var1, @NotNull EncodeShape var2) throws BackendException;

   void rebuildIndex(@NotNull StoreId var1) throws BackendException;

   @NotNull
   <R> SearchResult<R> execute(@NotNull StoreId var1, @NotNull EncodeShape var2, @NotNull SearchSpec var3) throws BackendException;
}

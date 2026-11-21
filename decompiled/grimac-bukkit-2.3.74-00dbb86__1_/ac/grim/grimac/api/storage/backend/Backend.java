package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.check.CheckCatalogPersistence;
import ac.grim.grimac.api.storage.check.CheckCatalogRepairResult;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Query;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@ApiStatus.Experimental
public interface Backend {
   @NotNull
   String id();

   @NotNull
   ApiVersion getApiVersion();

   @NotNull
   EnumSet<Capability> capabilities();

   @NotNull
   Set<Category<?>> supportedCategories();

   void init(@NotNull BackendContext var1) throws BackendException;

   @NotNull
   CheckCatalogPersistence checkCatalog();

   @NotNull
   CheckCatalogRepairResult repairCheckCatalog(@NotNull Map<Integer, Integer> var1, @Nullable String var2) throws BackendException;

   void flush() throws BackendException;

   void close() throws BackendException;

   @NotNull
   <E> StorageEventHandler<E> eventHandlerFor(@NotNull Category<E> var1) throws BackendException;

   @NotNull
   <R> Page<R> read(@NotNull Category<?> var1, @NotNull Query<R> var2) throws BackendException;

   <E> void delete(@NotNull Category<E> var1, @NotNull DeleteCriteria var2) throws BackendException;

   default <R> void bulkImport(@NotNull Category<?> cat, @NotNull List<R> records) throws BackendException {
      throw new UnsupportedOperationException(
         "backend " + this.id() + " does not support bulkImport; cannot be used as a V0 → V1 migration or cross-backend copy target"
      );
   }

   long countViolationsInSession(@NotNull UUID var1) throws BackendException;

   default long countUniqueChecksInSession(@NotNull UUID sessionId) throws BackendException {
      throw new UnsupportedOperationException("backend " + this.id() + " does not support countUniqueChecksInSession");
   }

   default long countSessionsByPlayer(@NotNull UUID player) throws BackendException {
      throw new UnsupportedOperationException("backend " + this.id() + " does not support countSessionsByPlayer");
   }

   default long markCrashedSessions() throws BackendException {
      return 0L;
   }
}

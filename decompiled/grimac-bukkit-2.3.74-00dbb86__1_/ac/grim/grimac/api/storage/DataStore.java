package ac.grim.grimac.api.storage;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.query.DeleteCriteria;
import ac.grim.grimac.api.storage.query.Page;
import ac.grim.grimac.api.storage.query.Query;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

@ApiStatus.Experimental
public interface DataStore {
   <E> void submit(@NotNull Category<E> var1, @NotNull Consumer<E> var2);

   @NotNull
   <R> CompletionStage<Page<R>> query(@NotNull Category<?> var1, @NotNull Query<R> var2);

   @NotNull
   default <R> CompletionStage<R> execute(@NotNull Operation<R> op) {
      throw new UnsupportedOperationException("execute(Operation) requires a V2-backed DataStoreImpl");
   }

   @NotNull
   <E> CompletionStage<Void> delete(@NotNull Category<E> var1, @NotNull DeleteCriteria var2);

   @NotNull
   CompletionStage<DeletionReport> forgetPlayer(@NotNull UUID var1);

   @NotNull
   CompletionStage<Long> countViolationsInSession(@NotNull UUID var1);

   @NotNull
   CompletionStage<Long> countUniqueChecksInSession(@NotNull UUID var1);

   @NotNull
   CompletionStage<Long> countSessionsByPlayer(@NotNull UUID var1);

   @NotNull
   DataStoreMetrics metrics();

   void flushAndClose(long var1);
}

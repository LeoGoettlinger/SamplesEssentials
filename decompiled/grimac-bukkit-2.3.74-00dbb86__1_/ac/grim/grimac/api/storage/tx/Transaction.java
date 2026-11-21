package ac.grim.grimac.api.storage.tx;

import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

@ApiStatus.Experimental
public interface Transaction {
   <E> void submit(@NotNull Category<E> var1, @NotNull Consumer<E> var2);

   <R> R execute(@NotNull Operation<R> var1) throws Exception;

   void rollback();
}

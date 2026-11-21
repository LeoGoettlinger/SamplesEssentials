package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.kind.Operation;
import ac.grim.grimac.api.storage.registry.Migration;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.EnumSet;
import java.util.List;

@ApiStatus.Experimental
public interface KindAdapter<K extends DataKind<?, ?>> {
   @NotNull
   Class<K> kindType();

   @NotNull
   EnumSet<Capability> subcapabilities();

   void ensureStore(@NotNull StoreId var1, @NotNull K var2) throws BackendException;

   void dropStore(@NotNull StoreId var1, @NotNull K var2) throws BackendException;

   @NotNull
   <E> StorageEventHandler<E> writeHandler(@NotNull StoreId var1, @NotNull K var2, @NotNull Category<E> var3);

   <R> R execute(@NotNull StoreId var1, @NotNull K var2, @NotNull Operation<R> var3) throws BackendException;

   @NotNull
   List<Migration<K>> migrations(@NotNull K var1);
}

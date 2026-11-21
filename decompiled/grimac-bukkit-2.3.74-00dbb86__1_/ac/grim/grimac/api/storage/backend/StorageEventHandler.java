package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;

@FunctionalInterface
@ApiStatus.Experimental
public interface StorageEventHandler<E> {
   void onEvent(E var1, long var2, boolean var4) throws BackendException;
}

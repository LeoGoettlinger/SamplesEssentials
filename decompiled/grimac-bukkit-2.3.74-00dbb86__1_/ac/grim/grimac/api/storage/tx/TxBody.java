package ac.grim.grimac.api.storage.tx;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@FunctionalInterface
@ApiStatus.Experimental
public interface TxBody<T> {
   T run(@NotNull Transaction var1) throws Exception;
}

package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.tx.TxBody;
import ac.grim.grimac.api.storage.tx.TxScope;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.EnumSet;

@ApiStatus.Experimental
public interface TxAdapter {
   @NotNull
   EnumSet<Capability> txCapabilities();

   <T> T run(@Nullable TxScope var1, @NotNull TxBody<T> var2) throws Exception;
}

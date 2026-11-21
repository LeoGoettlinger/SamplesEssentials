package ac.grim.grimac.api.storage.registry;

import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

@ApiStatus.Experimental
public interface Migration<K extends DataKind<?, ?>> {
   int fromVersion();

   int toVersion();

   void apply(@NotNull MigrationContext var1, @NotNull StoreId var2, @NotNull K var3) throws Exception;
}

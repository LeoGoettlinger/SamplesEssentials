package ac.grim.grimac.api.storage.backend;

import ac.grim.grimac.api.storage.category.Capability;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.instance.ServerOwnershipAdapter;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.EnumSet;
import java.util.Optional;

@ApiStatus.Experimental
public interface BackendV2 {
   @NotNull
   String id();

   @NotNull
   ApiVersion apiVersion();

   @NotNull
   EnumSet<Capability> capabilities();

   void init(@NotNull BackendContext var1) throws BackendException;

   void flush() throws BackendException;

   void close() throws BackendException;

   @NotNull
   <K extends DataKind<?, ?>> Optional<KindAdapter<K>> adapterFor(@NotNull K var1);

   @NotNull
   Optional<SearchAdapter> searchAdapter();

   @NotNull
   Optional<TxAdapter> txAdapter();

   @NotNull
   Optional<AdminAdapter> adminAdapter();

   @NotNull
   default Optional<ServerOwnershipAdapter> ownershipAdapter() {
      return Optional.empty();
   }

   @NotNull
   <X> Optional<X> unwrap(@NotNull Class<X> var1);

   default int writerThreads(@NotNull Category<?> category) {
      return 1;
   }
}

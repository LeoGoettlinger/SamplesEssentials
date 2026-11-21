package ac.grim.grimac.internal.storage.backend;

import ac.grim.grimac.api.storage.backend.BackendProvider;
import ac.grim.grimac.api.storage.backend.BackendRegistry;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public final class BackendRegistryImpl implements BackendRegistry {
   private final ConcurrentHashMap<String, BackendProvider> providers = new ConcurrentHashMap<>();

   @Override
   public void register(@NotNull BackendProvider provider) {
      Objects.requireNonNull(provider, "provider");
      String id = Objects.requireNonNull(provider.id(), "provider.id()");
      if (id.isBlank()) {
         throw new IllegalArgumentException("provider id must not be blank");
      } else {
         this.providers.put(id, provider);
      }
   }

   @Nullable
   @Override
   public BackendProvider lookup(@NotNull String id) {
      Objects.requireNonNull(id, "id");
      return this.providers.get(id);
   }

   @NotNull
   @Override
   public Set<String> registeredIds() {
      return Set.copyOf(this.providers.keySet());
   }
}

package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.backend.BackendV2;
import ac.grim.grimac.api.storage.backend.KindAdapter;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.api.storage.kind.DataKind;
import ac.grim.grimac.api.storage.registry.StoreId;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

@ApiStatus.Internal
public final class V2Routes {
   @NotNull
   private final Map<Category<?>, V2Routes.Route<?>> routes;

   private V2Routes(@NotNull IdentityHashMap<Category<?>, V2Routes.Route<?>> routes) {
      this.routes = routes;
   }

   @NotNull
   public static V2Routes.Builder builder() {
      return new V2Routes.Builder();
   }

   @NotNull
   public static V2Routes empty() {
      return new V2Routes(new IdentityHashMap<>());
   }

   public boolean contains(@NotNull Category<?> category) {
      return this.routes.containsKey(category);
   }

   @Nullable
   public <K extends DataKind<?, ?>> V2Routes.Route<K> routeFor(@NotNull Category<?> cat) {
      return (V2Routes.Route<K>)this.routes.get(cat);
   }

   public boolean isEmpty() {
      return this.routes.isEmpty();
   }

   public static final class Builder {
      private final Map<Category<?>, V2Routes.Route<?>> routes = new IdentityHashMap<>();

      @NotNull
      public <E, R, K extends DataKind<E, R>> V2Routes.Builder register(
         @NotNull Category<E> category, @NotNull StoreId storeId, @NotNull K kind, @NotNull BackendV2 backend
      ) {
         if (this.routes.containsKey(category)) {
            V2Routes.Route<?> existing = this.routes.get(category);
            throw new IllegalStateException(
               "duplicate v2 route registration for category "
                  + category.id()
                  + " (existing: storeId="
                  + existing.storeId()
                  + " kind="
                  + existing.kind().name()
                  + " backend="
                  + existing.backend().id()
                  + "; attempted: storeId="
                  + storeId
                  + " kind="
                  + kind.name()
                  + " backend="
                  + backend.id()
                  + ") — two backends claiming the same category is a wiring bug"
            );
         } else {
            KindAdapter<K> adapter = backend.<K>adapterFor(kind)
               .orElseThrow(() -> new IllegalArgumentException("backend " + backend.id() + " does not advertise an adapter for kind " + kind.name()));
            this.routes.put(category, new V2Routes.Route<>(storeId, backend, adapter, kind));
            return this;
         }
      }

      @NotNull
      public V2Routes build() {
         IdentityHashMap<Category<?>, V2Routes.Route<?>> snapshot = new IdentityHashMap<>(this.routes.size());
         snapshot.putAll(this.routes);
         return new V2Routes(snapshot);
      }
   }

   public record Route<K extends DataKind<?, ?>>(@NotNull StoreId storeId, @NotNull BackendV2 backend, @NotNull KindAdapter<K> adapter, @NotNull K kind) {
   }
}

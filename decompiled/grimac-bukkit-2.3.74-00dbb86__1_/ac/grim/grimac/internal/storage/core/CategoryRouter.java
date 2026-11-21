package ac.grim.grimac.internal.storage.core;

import ac.grim.grimac.api.storage.backend.Backend;
import ac.grim.grimac.api.storage.category.Category;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@ApiStatus.Internal
public final class CategoryRouter {
   private final Map<Category<?>, Backend> routing;
   private final Map<String, Backend> backendsById;

   public CategoryRouter(Map<Category<?>, Backend> routing) {
      this.routing = Map.copyOf(routing);
      LinkedHashMap<String, Backend> byId = new LinkedHashMap<>();

      for (Backend b : routing.values()) {
         byId.putIfAbsent(b.id(), b);
      }

      this.backendsById = Collections.unmodifiableMap(byId);
   }

   public Backend backendFor(Category<?> cat) {
      Backend b = this.routing.get(cat);
      if (b == null) {
         throw new IllegalArgumentException("no backend routed for category '" + cat.id() + "'");
      } else {
         return b;
      }
   }

   public Set<Backend> allBackends() {
      return new LinkedHashSet<>(this.backendsById.values());
   }

   public Set<Category<?>> routedCategories() {
      return this.routing.keySet();
   }

   public Map<Category<?>, Backend> routing() {
      return this.routing;
   }
}

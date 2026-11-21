package ac.grim.grimac.internal.storage.identity;

import ac.grim.grimac.api.storage.DataStore;
import ac.grim.grimac.api.storage.category.Categories;
import ac.grim.grimac.api.storage.identity.NameResolverLink;
import ac.grim.grimac.api.storage.model.PlayerIdentity;
import ac.grim.grimac.api.storage.query.Queries;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApiStatus.Internal
public final class LocalCacheLink implements NameResolverLink {
   public static final String ID = "local-cache";
   private final DataStore store;

   public LocalCacheLink(DataStore store) {
      this.store = store;
   }

   @Override
   public String id() {
      return "local-cache";
   }

   @Override
   public CompletionStage<Optional<UUID>> resolveByName(String name) {
      return this.store
         .query(Categories.PLAYER_IDENTITY, Queries.getPlayerIdentityByName(name))
         .thenApply(page -> page.items().isEmpty() ? Optional.empty() : Optional.of(page.items().get(0).uuid()));
   }

   @Override
   public CompletionStage<Optional<String>> resolveByUuid(UUID uuid) {
      return this.store.query(Categories.PLAYER_IDENTITY, Queries.getPlayerIdentity(uuid)).thenApply(page -> {
         if (page.items().isEmpty()) {
            return Optional.empty();
         } else {
            PlayerIdentity id = page.items().get(0);
            return Optional.ofNullable(id.currentName());
         }
      });
   }
}

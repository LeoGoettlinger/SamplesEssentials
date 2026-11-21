package ac.grim.grimac.internal.storage.identity;

import ac.grim.grimac.api.storage.identity.NameResolver;
import ac.grim.grimac.api.storage.identity.NameResolverLink;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@ApiStatus.Internal
public final class NameResolverChain implements NameResolver {
   private final List<NameResolverLink> links;

   public NameResolverChain(List<NameResolverLink> links) {
      this.links = List.copyOf(links);
   }

   @Override
   public CompletionStage<Optional<UUID>> resolveByName(String name) {
      return this.chain(0, link -> link.resolveByName(name));
   }

   @Override
   public CompletionStage<Optional<String>> resolveByUuid(UUID uuid) {
      return this.chain(0, link -> link.resolveByUuid(uuid));
   }

   @Override
   public CompletionStage<List<UUID>> allHistoricalUsersOfName(String name) {
      return this.resolveByName(name).thenApply(opt -> opt.map(Collections::singletonList).orElseGet(Collections::emptyList));
   }

   private <T> CompletionStage<Optional<T>> chain(int idx, NameResolverChain.LinkAccess<T> access) {
      return idx >= this.links.size()
         ? CompletableFuture.completedStage(Optional.empty())
         : access.apply(this.links.get(idx))
            .thenCompose(result -> result.isPresent() ? CompletableFuture.completedStage((Optional<T>)result) : this.chain(idx + 1, access));
   }

   public List<NameResolverLink> links() {
      return new ArrayList<>(this.links);
   }

   @FunctionalInterface
   private interface LinkAccess<T> {
      CompletionStage<Optional<T>> apply(NameResolverLink var1);
   }
}

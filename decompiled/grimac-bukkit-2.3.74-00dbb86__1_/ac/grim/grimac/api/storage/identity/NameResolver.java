package ac.grim.grimac.api.storage.identity;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

@ApiStatus.Experimental
public interface NameResolver {
   @NotNull
   CompletionStage<Optional<UUID>> resolveByName(@NotNull String var1);

   @NotNull
   CompletionStage<Optional<String>> resolveByUuid(@NotNull UUID var1);

   @NotNull
   CompletionStage<List<UUID>> allHistoricalUsersOfName(@NotNull String var1);
}

package vrx.economy.vRXClans.libs.cloud.brigadier.node;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.immutables.value.Value.Immutable;
import vrx.economy.vRXClans.libs.cloud.brigadier.suggestion.SuggestionsType;

@API(
   status = API.Status.INTERNAL,
   since = "2.0.0"
)
@Immutable
interface ArgumentMapping<S> {
   @NonNull
   ArgumentType<?> argumentType();

   @NonNull
   default SuggestionsType suggestionsType() {
      return SuggestionsType.BRIGADIER_SUGGESTIONS;
   }

   @Nullable
   SuggestionProvider<S> suggestionProvider();
}

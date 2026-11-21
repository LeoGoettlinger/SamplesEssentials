package vrx.economy.vRXClans.libs.cloud.suggestion;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface BlockingSuggestionProvider<C> extends SuggestionProvider<C> {
   @NonNull
   Iterable<? extends Suggestion> suggestions(@NonNull CommandContext<C> context, @NonNull CommandInput input);

   @NonNull
   @Override
   default CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(
      @NonNull final CommandContext<C> context, @NonNull final CommandInput input
   ) {
      return CompletableFuture.completedFuture(this.suggestions(context, input));
   }

   @FunctionalInterface
   @API(
      status = API.Status.STABLE
   )
   public interface Strings<C> extends BlockingSuggestionProvider<C> {
      @NonNull
      Iterable<String> stringSuggestions(@NonNull CommandContext<C> commandContext, @NonNull CommandInput input);

      @NonNull
      @Override
      default Iterable<Suggestion> suggestions(@NonNull final CommandContext<C> context, @NonNull final CommandInput input) {
         return StreamSupport.stream(this.stringSuggestions(context, input).spliterator(), false).map(Suggestion::suggestion).collect(Collectors.toList());
      }
   }
}

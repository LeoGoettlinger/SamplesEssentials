package vrx.economy.vRXClans.libs.cloud.suggestion;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;

final class NoSuggestions implements SuggestionProvider<Object> {
   private static final SuggestionProvider<?> INSTANCE = new NoSuggestions();
   private final CompletableFuture<? extends Iterable<? extends Suggestion>> result = CompletableFuture.completedFuture(Collections.emptyList());

   private NoSuggestions() {
   }

   @NonNull
   @Override
   public CompletableFuture<? extends Iterable<? extends Suggestion>> suggestionsFuture(
      @NonNull final CommandContext<Object> context, @NonNull final CommandInput input
   ) {
      return this.result;
   }

   @NonNull
   static <C> SuggestionProvider<C> instance() {
      return (SuggestionProvider<C>)INSTANCE;
   }
}

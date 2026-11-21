package de.theredend2000.advancedhunt.lib.cloud.suggestion;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

final class MappingSuggestionFactory<C, S extends Suggestion> implements SuggestionFactory<C, S> {
   private final SuggestionFactory<C, ?> other;
   private final SuggestionMapper<S> suggestionMapper;

   MappingSuggestionFactory(@NonNull final SuggestionFactory<C, ?> other, @NonNull final SuggestionMapper<S> suggestionMapper) {
      this.other = other;
      this.suggestionMapper = suggestionMapper;
   }

   @NonNull
   @Override
   public CompletableFuture<Suggestions<C, S>> suggest(@NonNull final CommandContext<C> context, @NonNull final String input) {
      return this.map(this.other.suggest(context, input));
   }

   @NonNull
   @Override
   public CompletableFuture<Suggestions<C, S>> suggest(@NonNull final C sender, @NonNull final String input) {
      return this.map(this.other.suggest(sender, input));
   }

   @NonNull
   @Override
   public <S2 extends Suggestion> SuggestionFactory<C, S2> mapped(@NonNull final SuggestionMapper<S2> mapper) {
      return new MappingSuggestionFactory<>(this.other, this.suggestionMapper.then(mapper));
   }

   @NonNull
   private <S1 extends Suggestion> CompletableFuture<Suggestions<C, S>> map(@NonNull final CompletableFuture<Suggestions<C, S1>> future) {
      return future.thenApply(
         suggestions -> Suggestions.create(
            suggestions.commandContext(), suggestions.list().stream().map(this.suggestionMapper::map).collect(Collectors.toList()), suggestions.commandInput()
         )
      );
   }
}

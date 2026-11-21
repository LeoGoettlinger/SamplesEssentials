package de.oliver.cloud.suggestion;

import de.oliver.cloud.execution.preprocessor.CommandPreprocessingContext;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.checkerframework.checker.nullness.qual.NonNull;

@FunctionalInterface
@API(
   status = Status.STABLE
)
public interface SuggestionProcessor<C> {
   @NonNull
   static <C> SuggestionProcessor<C> passThrough() {
      return (ctx, suggestions) -> suggestions;
   }

   @NonNull
   Stream<Suggestion> process(@NonNull CommandPreprocessingContext<C> context, @NonNull Stream<Suggestion> suggestions);

   @NonNull
   default SuggestionProcessor<C> then(@NonNull final SuggestionProcessor<C> nextProcessor) {
      Objects.requireNonNull(nextProcessor, "nextProcessor");
      return new ChainedSuggestionProcessor<>(Arrays.asList(this, nextProcessor));
   }
}

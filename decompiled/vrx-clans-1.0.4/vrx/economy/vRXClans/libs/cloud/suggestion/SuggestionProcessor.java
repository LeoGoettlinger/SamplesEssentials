package vrx.economy.vRXClans.libs.cloud.suggestion;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.execution.preprocessor.CommandPreprocessingContext;

@FunctionalInterface
@API(
   status = API.Status.STABLE
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

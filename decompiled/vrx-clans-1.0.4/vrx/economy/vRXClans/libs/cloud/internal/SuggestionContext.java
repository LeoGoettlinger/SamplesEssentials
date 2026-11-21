package vrx.economy.vRXClans.libs.cloud.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.execution.preprocessor.CommandPreprocessingContext;
import vrx.economy.vRXClans.libs.cloud.suggestion.Suggestion;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionMapper;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProcessor;
import vrx.economy.vRXClans.libs.cloud.suggestion.Suggestions;

@API(
   status = API.Status.INTERNAL,
   consumers = {"vrx.economy.vRXClans.libs.cloud.*"}
)
public final class SuggestionContext<C, S extends Suggestion> {
   private final List<S> suggestions = new ArrayList<>();
   private final CommandPreprocessingContext<C> preprocessingContext;
   private final SuggestionMapper<S> mapper;
   private final SuggestionProcessor<C> processor;
   private final CommandContext<C> commandContext;

   public SuggestionContext(
      @NonNull final SuggestionProcessor<C> processor,
      @NonNull final CommandContext<C> commandContext,
      @NonNull final CommandInput commandInput,
      @NonNull final SuggestionMapper<S> mapper
   ) {
      this.processor = processor;
      this.commandContext = commandContext;
      this.preprocessingContext = CommandPreprocessingContext.of(this.commandContext, commandInput);
      this.mapper = mapper;
   }

   @NonNull
   public Suggestions<C, S> makeSuggestions() {
      Stream<S> stream = this.suggestions.stream();
      Stream<Suggestion> processedStream = this.processor.process(this.preprocessingContext, stream);
      List<S> list;
      if (stream == processedStream) {
         list = Collections.unmodifiableList(this.suggestions);
      } else {
         list = Collections.unmodifiableList(
            processedStream.peek(obj -> Objects.requireNonNull(obj, "suggestion")).map(this.mapper::map).collect(Collectors.toList())
         );
      }

      return Suggestions.create(this.commandContext, list, this.preprocessingContext.commandInput());
   }

   @NonNull
   public CommandContext<C> commandContext() {
      return this.commandContext;
   }

   public void addSuggestions(@NonNull final Iterable<? extends Suggestion> suggestions) {
      suggestions.forEach(this::addSuggestion);
   }

   public void addSuggestion(@NonNull final Suggestion suggestion) {
      Objects.requireNonNull(suggestion, "suggestion");
      this.suggestions.add(this.mapper.map(suggestion));
   }
}

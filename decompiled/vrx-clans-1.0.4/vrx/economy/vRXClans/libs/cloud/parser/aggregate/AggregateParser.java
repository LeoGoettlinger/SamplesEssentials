package vrx.economy.vRXClans.libs.cloud.parser.aggregate;

import io.leangen.geantyref.GenericTypeReflector;
import io.leangen.geantyref.TypeFactory;
import io.leangen.geantyref.TypeToken;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionVariable;
import vrx.economy.vRXClans.libs.cloud.caption.StandardCaptionKeys;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.exception.parsing.ParserException;
import vrx.economy.vRXClans.libs.cloud.key.CloudKey;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProvider;
import vrx.economy.vRXClans.libs.cloud.type.tuple.Pair;
import vrx.economy.vRXClans.libs.cloud.type.tuple.Triplet;

@API(
   status = API.Status.STABLE
)
public interface AggregateParser<C, O> extends ArgumentParser.FutureArgumentParser<C, O>, ParserDescriptor<C, O> {
   @NonNull
   static <C> AggregateParserBuilder<C> builder() {
      return new AggregateParserBuilder<>();
   }

   @NonNull
   static <C, U, V> AggregateParserPairBuilder<C, U, V, Pair<U, V>> pairBuilder(
      @NonNull final String firstName,
      @NonNull final ParserDescriptor<C, U> firstParser,
      @NonNull final String secondName,
      @NonNull final ParserDescriptor<C, V> secondParser
   ) {
      return new AggregateParserPairBuilder<>(
         CommandComponent.builder(firstName, firstParser).build(),
         CommandComponent.builder(secondName, secondParser).build(),
         AggregateParserPairBuilder.defaultMapper(),
         (TypeToken<Pair<U, V>>)TypeToken.get(
            TypeFactory.parameterizedClass(
               Pair.class, GenericTypeReflector.box(firstParser.valueType().getType()), GenericTypeReflector.box(secondParser.valueType().getType())
            )
         )
      );
   }

   @NonNull
   static <C, U, V, Z> AggregateParserTripletBuilder<C, U, V, Z, Triplet<U, V, Z>> tripletBuilder(
      @NonNull final String firstName,
      @NonNull final ParserDescriptor<C, U> firstParser,
      @NonNull final String secondName,
      @NonNull final ParserDescriptor<C, V> secondParser,
      @NonNull final String thirdName,
      @NonNull final ParserDescriptor<C, Z> thirdParser
   ) {
      return new AggregateParserTripletBuilder<>(
         CommandComponent.builder(firstName, firstParser).build(),
         CommandComponent.builder(secondName, secondParser).build(),
         CommandComponent.builder(thirdName, thirdParser).build(),
         AggregateParserTripletBuilder.defaultMapper(),
         (TypeToken<Triplet<U, V, Z>>)TypeToken.get(
            TypeFactory.parameterizedClass(
               Triplet.class,
               GenericTypeReflector.box(firstParser.valueType().getType()),
               GenericTypeReflector.box(secondParser.valueType().getType()),
               GenericTypeReflector.box(thirdParser.valueType().getType())
            )
         )
      );
   }

   @NonNull
   List<CommandComponent<C>> components();

   @NonNull
   AggregateResultMapper<C, O> mapper();

   @NonNull
   @Override
   default CompletableFuture<ArgumentParseResult<O>> parseFuture(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      AggregateParsingContext<C> aggregateParsingContext = AggregateParsingContext.argumentContext(this);
      CompletableFuture<ArgumentParseResult<Object>> future = CompletableFuture.completedFuture(null);

      for (CommandComponent<C> component : this.components()) {
         future = future.thenCompose(
            result -> {
               if (result != null && result.failure().isPresent()) {
                  return ArgumentParseResult.failureFuture(result.failure().get());
               } else {
                  commandInput.skipWhitespace(1);
                  return commandInput.isEmpty()
                     ? ArgumentParseResult.failureFuture(new AggregateParser.AggregateParseException(commandContext, component))
                     : component.parser().parseFuture(commandContext, commandInput).thenApply(value -> {
                        if (value.parsedValue().isPresent()) {
                           CloudKey key = CloudKey.of(component.name(), component.valueType());
                           aggregateParsingContext.store(key, value.parsedValue().get());
                        } else if (value.failure().isPresent()) {
                           return ArgumentParseResult.failure(new AggregateParser.AggregateParseException(commandContext, "", component, value.failure().get()));
                        }

                        return (ArgumentParseResult<Object>)value;
                     });
               }
            }
         );
      }

      return future.thenCompose(
         result -> (CompletionStage<ArgumentParseResult<O>>)(result != null && result.failure().isPresent()
            ? result.asFuture()
            : this.mapper().map(commandContext, aggregateParsingContext))
      );
   }

   @NonNull
   @Override
   default SuggestionProvider<C> suggestionProvider() {
      return new AggregateSuggestionProvider<>(this);
   }

   @NonNull
   @Override
   default ArgumentParser<C, O> parser() {
      return this;
   }

   @API(
      status = API.Status.STABLE
   )
   public static final class AggregateParseException extends ParserException {
      private AggregateParseException(
         @NonNull final CommandContext<?> context, @NonNull final String input, @NonNull final CommandComponent<?> component, @NonNull final Throwable cause
      ) {
         super(
            cause,
            AggregateParser.class,
            context,
            StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_COMPONENT_FAILURE,
            CaptionVariable.of("input", input),
            CaptionVariable.of("component", component.name()),
            CaptionVariable.of("failure", cause.getMessage())
         );
      }

      private AggregateParseException(@NonNull final CommandContext<?> context, @NonNull final CommandComponent<?> component) {
         super(
            AggregateParser.class,
            context,
            StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_AGGREGATE_MISSING_INPUT,
            CaptionVariable.of("component", component.name())
         );
      }
   }
}

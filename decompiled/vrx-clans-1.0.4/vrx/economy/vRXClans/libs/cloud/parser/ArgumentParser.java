package vrx.economy.vRXClans.libs.cloud.parser;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.parser.standard.EitherParser;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProvider;
import vrx.economy.vRXClans.libs.cloud.suggestion.SuggestionProviderHolder;
import vrx.economy.vRXClans.libs.cloud.type.Either;

@FunctionalInterface
@API(
   status = API.Status.STABLE
)
public interface ArgumentParser<C, T> extends SuggestionProviderHolder<C> {
   @NonNull
   ArgumentParseResult<T> parse(@NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput);

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   default CompletableFuture<ArgumentParseResult<T>> parseFuture(@NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput) {
      return CompletableFuture.completedFuture(this.parse(commandContext, commandInput));
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   default <O> ArgumentParser.FutureArgumentParser<C, O> flatMap(final MappedArgumentParser.Mapper<C, T, O> mapper) {
      return new MappedArgumentParserImpl<>(this, Objects.requireNonNull(mapper, "mapper"));
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   default <O> ArgumentParser.FutureArgumentParser<C, O> flatMapSuccess(
      @NonNull final BiFunction<CommandContext<C>, T, CompletableFuture<ArgumentParseResult<O>>> mapper
   ) {
      Objects.requireNonNull(mapper, "mapper");
      return this.flatMap((ctx, result) -> result.flatMapSuccessFuture(value -> mapper.apply(ctx, value)));
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   default <O> ArgumentParser.FutureArgumentParser<C, O> mapSuccess(@NonNull final BiFunction<CommandContext<C>, T, CompletableFuture<O>> mapper) {
      Objects.requireNonNull(mapper, "mapper");
      return this.flatMap((ctx, result) -> result.mapSuccessFuture(value -> mapper.apply(ctx, value)));
   }

   @NonNull
   @Override
   default SuggestionProvider<C> suggestionProvider() {
      return this instanceof SuggestionProvider ? (SuggestionProvider)this : SuggestionProvider.noSuggestions();
   }

   @NonNull
   static <C, U, V> ParserDescriptor<C, Either<U, V>> firstOf(@NonNull final ParserDescriptor<C, U> primary, @NonNull final ParserDescriptor<C, V> fallback) {
      return EitherParser.eitherParser(primary, fallback);
   }

   @FunctionalInterface
   @API(
      status = API.Status.STABLE
   )
   public interface FutureArgumentParser<C, T> extends ArgumentParser<C, T> {
      @NonNull
      @Override
      default ArgumentParseResult<T> parse(@NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput) {
         throw new UnsupportedOperationException("parse should not be called on a FutureArgumentParser. Call parseFuture instead.");
      }

      @NonNull
      @Override
      CompletableFuture<ArgumentParseResult<T>> parseFuture(@NonNull CommandContext<C> commandContext, @NonNull CommandInput commandInput);
   }
}

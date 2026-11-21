package de.theredend2000.advancedhunt.lib.cloud.parser.aggregate;

import de.theredend2000.advancedhunt.lib.cloud.context.CommandContext;
import de.theredend2000.advancedhunt.lib.cloud.parser.ArgumentParseResult;
import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;

@API(
   status = API.Status.STABLE
)
public interface AggregateResultMapper<C, O> {
   @NonNull
   CompletableFuture<ArgumentParseResult<O>> map(@NonNull CommandContext<C> commandContext, @NonNull AggregateParsingContext<C> context);

   @API(
      status = API.Status.STABLE
   )
   public interface DirectSuccessMapper<C, O> extends AggregateResultMapper<C, O> {
      @NonNull
      O mapSuccess(@NonNull CommandContext<C> commandContext, @NonNull AggregateParsingContext<C> context);

      @NonNull
      @Override
      default CompletableFuture<ArgumentParseResult<O>> map(@NonNull CommandContext<C> commandContext, @NonNull AggregateParsingContext<C> context) {
         return ArgumentParseResult.successFuture(this.mapSuccess(commandContext, context));
      }
   }
}

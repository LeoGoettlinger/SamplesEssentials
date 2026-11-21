package vrx.economy.vRXClans.libs.cloud.parser.aggregate;

import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;

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

package vrx.economy.vRXClans.libs.cloud.parser;

import java.util.concurrent.CompletableFuture;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;

@API(
   status = API.Status.STABLE
)
public interface MappedArgumentParser<C, I, O> extends ArgumentParser<C, O> {
   @NonNull
   ArgumentParser<C, I> baseParser();

   @FunctionalInterface
   public interface Mapper<C, I, O> {
      @NonNull
      CompletableFuture<ArgumentParseResult<O>> map(@NonNull CommandContext<C> context, @NonNull ArgumentParseResult<I> input);
   }
}
